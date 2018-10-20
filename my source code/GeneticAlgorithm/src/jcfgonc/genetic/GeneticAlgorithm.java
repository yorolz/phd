package jcfgonc.genetic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import jcfgonc.genetic.operators.GeneticOperations;
import jcfgonc.genetic.threads.EpochEvolverThread;
import jcfgonc.genetic.threads.FitnessEvaluatingThread;
import jcfgonc.genetic.threads.PopulationInitializerThread;
import structures.CSVWriter;
import structures.MovingAverage;
import structures.Ticker;

/**
 * @author CK
 */

public class GeneticAlgorithm<T> {

	private final int amountThreads;
	private ExecutorService es;
	private Chromosome<T>[] population, nextPopulation;
	private int populationSize;
	private double crossoverProbability;
	private int maximumGenerations;
	private double mutationProbability;
	private double mutationProbabilityTarget;
	private RandomGenerator[] randomGenerator;
	private EvolutionChart evolutionWindow;
	private int tournamentSize;
	private double tournamentStrongestProb;
	private double tournamentStrongestProbTarget;
	private GeneticOperations<T> geneOperator;
	private CSVWriter csvw;

	public GeneticAlgorithm(GeneticOperations<T> geneOperator, CSVWriter csvw) throws IOException {
		this(geneOperator);
		this.csvw = csvw;
		if (csvw != null) {
			this.csvw.setHeader("currentGeneration", "elapsedTime", "overallBestFitness", "currentBestFitness", "currentMedianFitness", "current1stQuarterFitness",
					"currentDiversity");
		}
	}

	@SuppressWarnings("unchecked")
	public GeneticAlgorithm(GeneticOperations<T> geneOperator) {
		int nThreads = GeneticAlgorithmConfig.NUMBER_OF_THREADS;
		if (nThreads < 1)
			nThreads = 1;
		// initialize
		this.amountThreads = nThreads;
		this.maximumGenerations = GeneticAlgorithmConfig.MAXIMUM_GENERATIONS;
		this.populationSize = (int) roundUp(GeneticAlgorithmConfig.POPULATION_SIZE, 2);
		this.geneOperator = geneOperator;
		this.population = new Chromosome[populationSize];
		this.nextPopulation = new Chromosome[populationSize];
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

	private double clipValue(double value) {
		if (value > 1)
			value = 1;
		else if (value < 0)
			value = 0;
		return value;
	}

	private void evaluatePopulationFitnessP() throws InterruptedException {
		// create queue of chromosomes to be processed by the fitness threads
		ConcurrentLinkedQueue<Chromosome<T>> chromosomeQueue = new ConcurrentLinkedQueue<>();
		for (Chromosome<T> c : this.population) {
			chromosomeQueue.add(c);
		}

		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		int range_size = this.populationSize / this.amountThreads;
		for (int thread_id = 0; thread_id < this.amountThreads; thread_id++) {
			int range_l = range_size * thread_id;
			int range_h;
			if (thread_id == this.amountThreads - 1)
				range_h = this.populationSize - 1;
			else
				range_h = range_size * (thread_id + 1) - 1;
//			FitnessEvaluatingThread<T> command = new FitnessEvaluatingThread<T>(thread_id, this.population, range_l, range_h, geneOperator);
			FitnessEvaluatingThread<T> command = new FitnessEvaluatingThread<T>(thread_id, chromosomeQueue, range_l, range_h, geneOperator);
			Callable<T> call = new Callable<T>() {
				@Override
				public T call() {
					command.run();
					return null;
				}
			};
			tasks.add(call);
		}
		// the code above takes about 1...2 ms

		es.invokeAll((Collection<? extends Callable<T>>) tasks);
		chromosomeQueue.clear();
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
			EpochEvolverThread<T> e = new EpochEvolverThread<T>(rangeL, rangeH, population, nextPopulation, mutationProbability, randomGenerator[threadId], crossoverProbability,
					tournamentSize, tournamentStrongestProb, geneOperator);
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
	 * @throws IOException
	 */
	public void execute() throws InterruptedException, IOException {
		setupEvolutionWindow();

		System.out.println("Genetic Algorithm started.");
		Chromosome<T> overallBestChromosome = null;
		Ticker ticker = new Ticker();

		initializePopulation(population, 0, populationSize);
		evaluatePopulationFitnessP();

		// execute
		int currentGeneration = 0;
		MovingAverage worstAvg = new MovingAverage(4);

		while (currentGeneration < maximumGenerations && evolutionWindow.allowExecution() && ticker.getElapsedTime() < GeneticAlgorithmConfig.MAXIMUM_TIME_SECONDS) {

			evolve();
			evaluatePopulationFitnessP();

			// sort population into ascending fitness order
			if (populationSize >= 1 << 16) {
				Arrays.parallelSort(population);
			} else {
				Arrays.sort(population);
			}

			Chromosome<T> currentBestChromosome = population[population.length - 1];
			if (overallBestChromosome == null || currentBestChromosome.compareTo(overallBestChromosome) > 0) {
				// copy because it may be modified in the future
				T geneCopy = geneOperator.createGeneCopy(currentBestChromosome.getGenes(), false);
				overallBestChromosome = new Chromosome<>(geneCopy);
				overallBestChromosome.updateFitness(geneOperator);
				evolutionWindow.updateBestCandidate(overallBestChromosome.getGenes());
			}

			double overallBestFitness = overallBestChromosome.getFitness();
			double currentBestFitness = currentBestChromosome.getFitness();
			double currentMedianFitness = population[proportionOfInt(population.length, 0.5)].getFitness();
			double current1stQuarterFitness = population[proportionOfInt(population.length, 0.25)].getFitness();
			double currentDiversity = getPopulationDiversity(0.75);
			worstAvg.add(current1stQuarterFitness);

			evolutionWindow.addEpoch(currentGeneration, overallBestFitness, currentBestFitness, currentMedianFitness, worstAvg.getMean());
			evolutionWindow.updateGeneticAlgorithmStats(currentGeneration, tournamentStrongestProb, mutationProbability, currentDiversity);
			evolutionWindow.repaint();

			if (csvw != null) {
				csvw.addLine(Integer.toString(currentGeneration), Double.toString(ticker.getElapsedTime()), Double.toString(overallBestFitness),
						Double.toString(currentBestFitness), Double.toString(currentMedianFitness), Double.toString(current1stQuarterFitness), Double.toString(currentDiversity));
				csvw.flush();
			}

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

		if (ticker.getElapsedTime() >= GeneticAlgorithmConfig.MAXIMUM_TIME_SECONDS) {
			System.out.println("timeout!");
		}

		// sort population into ascending fitness order
		Arrays.parallelSort(population);
		System.out.println("Genetic Algorithm executed.");
		// Thread.sleep(1000);
		evolutionWindow.dispose();
	}

	private double getPopulationDiversity(double fromRatio) {
		double sum = 0;
		double sq_sum = 0;
		final int i0 = proportionOfInt(population.length, fromRatio);
		final int i1 = populationSize;
		double di = i1 - i0;
		for (int i = i0; i < i1; i++) {
			double ai = population[i].getFitness();
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
	public T getBestGenes() {
		Chromosome<T> best = getChromosome(population.length - 1);
		return best.getGenes();
	}

	public Chromosome<T> getChromosome(int individual) {
		return population[individual];
	}

	public ArrayList<T> getPopulationDescendingFitness(int amount) {
		ArrayList<T> pop = new ArrayList<>();
		// ga population is in ascending order, revert so that the first element is the highest
		for (int i = 0; i < amount; i++) {
			T b = getGenes(populationSize - i - 1);
			pop.add(b);
		}
		return pop;
	}

	public T getGenes(int individual) {
		return getChromosome(individual).getGenes();
	}

	public int getMaximumGenerations() {
		return maximumGenerations;
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
			Callable<T> call = new PopulationInitializerThread<T>(pop, rangeL, rangeH, randomGenerator[threadId], geneOperator);
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

	public void showCurrentPopulation() {
		for (int i = 0; i < populationSize; i++) {
			Chromosome<T> chromosome = getChromosome(i);
			System.out.println(chromosome);
		}
	}
}
