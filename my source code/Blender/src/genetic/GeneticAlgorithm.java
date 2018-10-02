package genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import genetic.operators.FitnessEvaluator;
import genetic.operators.GeneCrossover;
import genetic.operators.GeneInitializer;
import genetic.operators.GeneMutation;
import genetic.operators.GeneRepair;
import genetic.threads.EpochEvolverThread;
import genetic.threads.FitnessEvaluatingThread;
import genetic.threads.PopulationInitializerThread;
import structures.MovingAverage;
import structures.Ticker;

/**
 * @author CK
 */

public class GeneticAlgorithm<T> {

	private final int amountThreads;
	private List<List<T>> baseChromosomes;
	private ExecutorService es;
	private FitnessEvaluator<T> fitnessEvaluator;
	private GeneInitializer<T> geneInitializer;
	private GeneCrossover<T> geneCrossover;
	private GeneMutation<T> geneMutator;
	private final int numberOfGenes;
	private Chromosome<T>[] population, nextPopulation;
	private int populationSize;
	private GeneRepair<T> repairingOperator;
	private double crossoverProbability;
	private int maximumGenerations;
	private double mutationProbability;
	private double mutationProbabilityTarget;
	private Class<T> geneClass;
	private RandomGenerator[] randomGenerator;
	private EvolutionChart evolutionWindow;
	private int tournamentSize;
	private double tournamentStrongestProb;
	private double tournamentStrongestProbTarget;

	@SuppressWarnings("unchecked")
	public GeneticAlgorithm(Class<T> geneclass, GeneMutation<T> geneMutator, FitnessEvaluator<T> fitnessEvaluator, GeneCrossover<T> geneCrossover,
			GeneInitializer<T> geneInitializer, int numberOfGenesPerChromosomes) {
		int nThreads = GeneticAlgorithmConfig.NUMBER_OF_THREADS;
		if (nThreads < 1)
			nThreads = 1;
		// initialize
		this.amountThreads = nThreads;
		this.maximumGenerations = GeneticAlgorithmConfig.MAXIMUM_GENERATIONS;
		this.populationSize = (int) roundUp(GeneticAlgorithmConfig.POPULATION_SIZE, 2);
		this.geneClass = geneclass;
		this.fitnessEvaluator = fitnessEvaluator;
		this.numberOfGenes = numberOfGenesPerChromosomes;
		this.geneMutator = geneMutator;
		this.geneInitializer = geneInitializer;
		this.geneCrossover = geneCrossover;
		this.population = new Chromosome[populationSize];
		this.nextPopulation = new Chromosome[populationSize];
		this.baseChromosomes = new ArrayList<>();
		this.es = Executors.newFixedThreadPool(this.amountThreads);

		this.crossoverProbability = clipValue(GeneticAlgorithmConfig.CROSSOVER_PROBABILITY);

		this.mutationProbabilityTarget = clipValue(GeneticAlgorithmConfig.MUTATION_PROBABILITY);
		this.mutationProbability = mutationProbabilityTarget;

		this.tournamentSize = GeneticAlgorithmConfig.TOURNAMENT_SIZE;

		this.tournamentStrongestProbTarget = GeneticAlgorithmConfig.TOURNAMENT_STRONGEST_PROBABILITY;
		this.tournamentStrongestProb = tournamentStrongestProbTarget;

		this.randomGenerator = new RandomGenerator[amountThreads];
		Ticker t = new Ticker();
		for (int i = 0; i < amountThreads; i++) {
			if (GeneticAlgorithmConfig.DETERMINISTIC) {
				this.randomGenerator[i] = new Well44497b(i * (1 << 24));
			} else {
				double elapsedTime = t.getElapsedTime();
				int seed = (int) (elapsedTime * (1 << 24));
				this.randomGenerator[i] = new Well44497b(seed + i);
			}
		}
	}

	/**
	 * Allows the user to give a base chromosome to be present in the first population/epoch. This function can be called multiple times to add multiple chromosomes.
	 *
	 * @param baseChromosome
	 */
	public void addInitialChromosomeGenes(List<T> baseChromosome) {
		this.baseChromosomes.add(baseChromosome);
	}

	private void addTemplateChromosomes(Chromosome<T>[] pop) {
		// initialize with template chromosomes, if given (replacing existing
		// chromosomes)
		for (int i = 0; i < baseChromosomes.size(); i++) {
			List<T> baseChromosome = baseChromosomes.get(i);
			// create a dummy chromosome
			Chromosome<T> c = new Chromosome<T>(geneClass, numberOfGenes);
			for (int j = 0; j < numberOfGenes; j++) {
				T gene = baseChromosome.get(i);
				c.genes[i] = gene;
			}
			pop[i] = c;
		}
	}

	private double clipValue(double value) {
		if (value > 1)
			value = 1;
		else if (value < 0)
			value = 0;
		return value;
	}

	private void evaluatePopulationFitnessP() throws InterruptedException {
		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		int range_size = this.populationSize / this.amountThreads;
		for (int thread_id = 0; thread_id < this.amountThreads; thread_id++) {
			int range_l = range_size * thread_id;
			int range_h;
			if (thread_id == this.amountThreads - 1)
				range_h = this.populationSize - 1;
			else
				range_h = range_size * (thread_id + 1) - 1;
			FitnessEvaluatingThread<T> command = new FitnessEvaluatingThread<T>(thread_id, this.population, range_l, range_h, fitnessEvaluator);
			Callable<T> call = new Callable<T>() {
				@Override
				public T call() {
					command.run();
					return null;
				}
			};
			tasks.add(call);
		}

		es.invokeAll((Collection<? extends Callable<T>>) tasks);
	}

	/**
	 * Executes one evolution (creates a new generation) of GA's algorithm.
	 *
	 * @param shakeNextGeneration
	 * @throws InterruptedException
	 */
	private void evolve() throws InterruptedException {

		// reproduce population with random chromosomes using parallel tasks
		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		int startI = 0;
		int endI = populationSize;

		int rangeSize = (endI - startI) / amountThreads;
		int rangeL = startI;
		int rangeH = rangeL + rangeSize;
		for (int threadId = 0; threadId < amountThreads; threadId++) {
			EpochEvolverThread<T> e = new EpochEvolverThread<T>(rangeL, rangeH, population, nextPopulation, geneCrossover, geneMutator, mutationProbability,
					randomGenerator[threadId], repairingOperator, crossoverProbability, tournamentSize, tournamentStrongestProb);
			tasks.add(e);
			rangeL += rangeSize;
			if (threadId == amountThreads - 2)
				rangeH = endI;
			else
				rangeH += rangeSize;
		}
		// invoke parallel tasks
		es.invokeAll((Collection<? extends Callable<T>>) tasks);

		// replace current/old population with new
		{
			Chromosome<T>[] temp = this.population;
			this.population = nextPopulation;
			this.nextPopulation = temp;
		}
	}

	/**
	 * Executes the GA until one of it's terminating criteria is reached.
	 *
	 * @throws InterruptedException
	 */
	public void execute() throws InterruptedException {
		setupEvolutionWindow();

		System.out.println("Genetic Algorithm started.");
		Chromosome<T> overallBestChromosome = null;

		initializePopulation(population, 0, populationSize);
		addTemplateChromosomes(population);
		evaluatePopulationFitnessP();

		// execute
		int currentGeneration = 0;
		MovingAverage worstAvg = new MovingAverage(4);

		while (currentGeneration < maximumGenerations && evolutionWindow.allowExecution()) {

			evolve();
			evaluatePopulationFitnessP();

			// sort population into ascending fitness order
			if (populationSize >= 8192) {
				Arrays.parallelSort(population);
			} else {
				Arrays.sort(population);
			}

			Chromosome<T> currentBestChromosome = population[population.length - 1];
			if (overallBestChromosome == null || currentBestChromosome.compareTo(overallBestChromosome) > 0) {
				// copy because it may be modified in the future
				overallBestChromosome = new Chromosome<>(currentBestChromosome, true);
				overallBestChromosome.updateFitness(fitnessEvaluator);
				evolutionWindow.updateBestCandidate(overallBestChromosome.genes[0]);
			}

			double overallBestFitness = overallBestChromosome.getFitness()[0];
			double currentBestFitness = currentBestChromosome.getFitness()[0];
			double currentMedianFitness = population[proportionOfInt(population.length, 0.5)].getFitness()[0];
			double current1stQuarterFitness = population[proportionOfInt(population.length, 0.25)].getFitness()[0];
			double currentDiversity = getPopulationDiversity(0.75);
			worstAvg.add(current1stQuarterFitness);

			evolutionWindow.addEpoch(currentGeneration, overallBestFitness, currentBestFitness, currentMedianFitness, worstAvg.getMean());
			evolutionWindow.updateGeneticAlgorithmStats(currentGeneration, tournamentStrongestProb, mutationProbability, currentDiversity);
			evolutionWindow.repaint();

			final double tournDelta = GeneticAlgorithmConfig.TOURNAMENT_DELTA;
			final double mutDelta = GeneticAlgorithmConfig.MUTATION_DELTA;

			if (currentDiversity < GeneticAlgorithmConfig.DIVERSITY_REQUIRED) {
				tournamentStrongestProb -= tournamentStrongestProb * tournDelta;
				mutationProbability += mutationProbability * mutDelta;
			} else {
				tournamentStrongestProb += tournamentStrongestProb * tournDelta;
				mutationProbability -= mutationProbability * mutDelta;
			}

			tournamentStrongestProb = clipValue(tournamentStrongestProb);
			mutationProbability = clipValue(mutationProbability);
			// System.out.printf("%g \t %f \t %f \n", currentDiversity, tournamentStrongestProb, mutationProbability);

			// tournamentStrongestProb += (tournamentStrongestProbTarget - tournamentStrongestProb) * 0.05;
			// mutationProbability += (mutationProbabilityTarget - mutationProbability) * 0.05;

			currentGeneration++;
		}
		es.shutdown();
		if (overallBestChromosome != null)
			population[population.length - 1] = overallBestChromosome; // store the best of all execution
		// sort population into ascending fitness order
		Arrays.parallelSort(population);
		System.out.println("Genetic Algorithm executed.");
		Thread.sleep(1000);
		evolutionWindow.dispose();
	}

	private double getPopulationDiversity(double fromRatio) {
		double sum = 0;
		double sq_sum = 0;
		final int i0 = proportionOfInt(population.length, fromRatio);
		final int i1 = populationSize;
		double di = i1 - i0;
		for (int i = i0; i < i1; i++) {
			double ai = population[i].getFitness()[0];
			sum += ai;
			sq_sum += ai * ai;
		}
		double mean = sum / di;
		double variance = (sq_sum / di) - (mean * mean);
		double res = Math.sqrt(Math.abs(variance));
		return res;
	}

	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	@SuppressWarnings("unused")
	private Chromosome<T> getBestChromosomeUsingTradefOff(double[] td) {
		int besti = 0;
		double bestTD = td[0];
		for (int i = 1; i < td.length; i++) {
			if (td[i] > bestTD) {
				bestTD = td[i];
				besti = 0;
			}
		}
		return population[besti];
	}

	@SuppressWarnings("unused")
	private double[] calculateTradeOff() {
		double[] tradeOffOptimality = new double[population.length];
		int noObjectives = population[0].getFitness().length;
		double[] maximumValue = new double[noObjectives];
		double[] minimumValue = new double[noObjectives];

		for (int i = 0; i < noObjectives; i++) {
			maximumValue[i] = Double.NEGATIVE_INFINITY;
			minimumValue[i] = Double.POSITIVE_INFINITY;
		}

		for (int i = 0; i < population.length; i++) {
			double[] fitness = population[i].getFitness();
			for (int j = 0; j < noObjectives; j++) {
				if (fitness[j] > maximumValue[j]) {
					maximumValue[j] = fitness[j];
				}
				if (fitness[j] < minimumValue[j]) {
					minimumValue[j] = fitness[j];
				}
			}
		}

		// for each chromosome, normalize metrics and calculate distance to maximum combination
		for (int i = 0; i < population.length; i++) {
			double[] fitness = population[i].getFitness();
			double sum = 0;
			for (int j = 0; j < noObjectives; j++) {
				double metric = (fitness[j] - minimumValue[j]) / (maximumValue[j] - minimumValue[j]);
				sum += metric;
			}
			tradeOffOptimality[i] = sum;
		}
		return tradeOffOptimality;
	}

	private void setupEvolutionWindow() {
		evolutionWindow = new EvolutionChart(GeneticAlgorithmConfig.EVOLUTION_WINDOW_SIZE);
		evolutionWindow.show();
	}

	public int getAmountThreads() {
		return amountThreads;
	}

	/**
	 * Returns the best genes from the last generation.
	 *
	 * @return
	 */
	public T[] getBestGenes() {
		Chromosome<T> bestChromosome = population[population.length - 1];
		return bestChromosome.genes;
	}

	public Chromosome<T> getChromosome(int individual) {
		return population[individual];
	}

	public T[] getGenes(int individual) {
		return population[individual].genes;
	}

	public int getMaximumGenerations() {
		return maximumGenerations;
	}

	public int getNumberOfGenes() {
		return numberOfGenes;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * Initializes the given population with random chromosomes. Goes from startI to endI (exclusive): [startI, endI[
	 *
	 * @throws InterruptedException
	 */
	private void initializePopulation(Chromosome<T>[] pop, int startI, int endI) throws InterruptedException {
		System.out.println("Genetic Algorithm initializing.");

		// create population with random chromosomes using parallel tasks
		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		int rangeSize = (endI - startI) / amountThreads;
		int rangeL = startI;
		int rangeH = rangeL + rangeSize;
		for (int threadId = 0; threadId < amountThreads; threadId++) {
			Callable<T> call = new PopulationInitializerThread<T>(pop, rangeL, rangeH, numberOfGenes, randomGenerator[threadId], repairingOperator, geneInitializer, geneClass);
			tasks.add(call);
			rangeL += rangeSize;
			if (threadId == amountThreads - 2)
				rangeH = endI;
			else
				rangeH += rangeSize;
		}
		// invoke parallel tasks
		es.invokeAll((Collection<? extends Callable<T>>) tasks);
	}

	/**
	 *
	 * @param x
	 *            value to be rounded
	 * @param f
	 *            the multiple
	 * @return nearest multiple of f to value x
	 */
	private double roundUp(double x, double f) {
		return f * Math.ceil(x / f);
	}

	public void setMaximumGenerations(int maximumGenerations) {
		this.maximumGenerations = maximumGenerations;
	}

	public void setRepairingOperator(GeneRepair<T> repairingOperator) {
		this.repairingOperator = repairingOperator;
	}

	public void showCurrentPopulation() {
		for (int i = 0; i < populationSize; i++) {
			Chromosome<T> chromosome = getChromosome(i);
			System.out.println(chromosome);
		}
	}
}
