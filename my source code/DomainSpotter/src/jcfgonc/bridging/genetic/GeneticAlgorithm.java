package jcfgonc.bridging.genetic;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import jcfgonc.bridging.genetic.operators.FitnessEvaluator;
import jcfgonc.bridging.genetic.operators.GeneCrossover;
import jcfgonc.bridging.genetic.operators.GeneInitializer;
import jcfgonc.bridging.genetic.operators.GeneMutation;
import jcfgonc.bridging.genetic.operators.GeneRepair;
import jcfgonc.bridging.genetic.threads.EpochEvolverThread;
import jcfgonc.bridging.genetic.threads.FitnessEvaluatingThread;
import jcfgonc.bridging.genetic.threads.PopulationInitializerThread;

/**
 * The Genetic Algorithm used to generate hardware configurations.
 * 
 * @author CK
 */

public class GeneticAlgorithm<T> {

	private static final boolean SHOW_STEPS = false;

	private static final int LARGE_PRIME = 433494437;

	private final int amountThreads;
	private final ExecutorService es;
	private final FitnessEvaluator<T> fitnessEvaluator;
	private final GeneInitializer<T> geneInitializer;
	private final GeneCrossover<T> geneCrossover;
	private final GeneMutation<T> geneMutator;
	private final int numberOfGenes;
	private Chromosome<T>[] population, nextPopulation;
	private int populationSize;
	private GeneRepair<T> repairingOperator;
	private int maximumGenerations;
	private double randomPopulationPercentange;
	private final DecimalFormat df = new DecimalFormat("#0.00");
	private ArrayList<RandomGenerator> threadRNGs;
	private final Class<T> geneClass;

	@SuppressWarnings("unchecked")
	public GeneticAlgorithm(Class<T> geneclass, GeneMutation<T> geneMutator, FitnessEvaluator<T> fitnessEvaluator, GeneCrossover<T> geneCrossover,
			GeneInitializer<T> geneInitializer, int numberOfGenesPerChromosomes, int gaMaximumGenerations, int gaPopulationSize) {
		int nThreads = GeneticAlgorithmConfig.GA_NUMBER_OF_THREADS;
		if (nThreads < 1)
			nThreads = 1;
		// initialize
		this.amountThreads = nThreads;
		this.maximumGenerations = gaMaximumGenerations;
		this.randomPopulationPercentange = GeneticAlgorithmConfig.GA_RANDOM_GENERATION_PERCENTAGE;
		this.populationSize = (int) roundUp(gaPopulationSize, 2);
		this.geneClass = geneclass;
		this.fitnessEvaluator = fitnessEvaluator;
		this.numberOfGenes = numberOfGenesPerChromosomes;
		this.geneMutator = geneMutator;
		this.geneInitializer = geneInitializer;
		this.geneCrossover = geneCrossover;
		this.population = new Chromosome[populationSize];
		this.nextPopulation = new Chromosome[populationSize];
		this.es = Executors.newFixedThreadPool(this.amountThreads);
		this.randomPopulationPercentange = clipValue(randomPopulationPercentange);
		this.threadRNGs = new ArrayList<RandomGenerator>();
		for (int i = 0; i < amountThreads; i++) {
			Well44497b w = new Well44497b(System.nanoTime() + LARGE_PRIME * i);
			// Well44497b w = new Well44497b(LARGE_PRIME * i);
			threadRNGs.add(w);
		}
		df.setRoundingMode(RoundingMode.FLOOR);
	}

	private double clipValue(double value) {
		if (value > 1)
			value = 1;
		else if (value < 0)
			value = 0;
		return value;
	}

	private void evaluatePopulationFitnessP() throws InterruptedException {
		if (GeneticAlgorithm.SHOW_STEPS) {
			System.out.println("evaluating population");
			System.out.flush();
		}

		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		final int range_size = this.populationSize / this.amountThreads;
		for (int thread_id = 0; thread_id < this.amountThreads; thread_id++) {
			final int range_l = range_size * thread_id;
			final int range_h;
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

		/**
		 * [New generation] Create a new population by repeating the following steps until the new population is complete
		 **/

		int howManyProcreate = populationSize;

		if (GeneticAlgorithm.SHOW_STEPS) {
			System.out.println("reproducing population");
			System.out.flush();
		}

		// reproduce population with random chromosomes using parallel tasks
		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		final int startI = 0;
		final int endI = howManyProcreate;

		int rangeSize = (endI - startI) / amountThreads;
		int rangeL = startI;
		int rangeH = rangeL + rangeSize;
		for (int threadId = 0; threadId < amountThreads; threadId++) {
			tasks.add(new EpochEvolverThread<>(rangeL, rangeH, population, nextPopulation, geneCrossover, geneMutator, threadRNGs.get(threadId),
					repairingOperator));
			rangeL += rangeSize;
			if (threadId == amountThreads - 2)
				rangeH = endI;
			else
				rangeH += rangeSize;
		}
		// invoke parallel tasks
		es.invokeAll((Collection<? extends Callable<T>>) tasks);

		// now, new_population has all the chromosomes of the new era
		// replace current population with new
		{
			// sort of double buffer swap
			Chromosome<T>[] temp = this.population;
			this.population = nextPopulation;
			this.nextPopulation = temp;
		}

		evaluatePopulationFitnessP();
	}

	/**
	 * double worstFitness = stats[0]; double bestFitness = stats[1]; double meanFitness = stats[2]; double stdFitness = stats[3];
	 * 
	 * @return
	 */
	public double[] getPopulationStatistics() {
		GeneticPopulationStatistics<T> gps = new GeneticPopulationStatistics<>();
		final double[] stats = gps.populationStats(population);
		return stats;
	}

	/**
	 * Executes the GA until one of it's terminating criteria is reached.
	 * 
	 * @throws InterruptedException
	 */
	public void execute() throws InterruptedException {
		Chromosome<T> overallBestChromosome = null;
		GeneticPopulationStatistics<T> gps = new GeneticPopulationStatistics<>();

		/** [Start] Generate random population of chromosomes, that is, suitable solutions for the problem. **/
		initializePopulation(population, 0, populationSize);
		/** [Fitness] Evaluate the fitness of each chromosome in the population. **/
		evaluatePopulationFitnessP();

		// execute
		int currentGeneration = 0;
		double bestFitness = 0;
		/** [Test] If the end condition is satisfied, stop, and return the best solution in current population **/
		while (currentGeneration < maximumGenerations) {

			/**
			 * [New population] Create a new population by repeating following steps until the new population is complete.
			 **/
			if (currentGeneration == 0) {
				// generation 0 is the initial, static
			} else {
				evolve();
			}

			if (GeneticAlgorithm.SHOW_STEPS) {
				System.out.println("sorting population");
				System.out.flush();
			}

			// sort population into ascending fitness order
			Arrays.parallelSort(population);

			if (GeneticAlgorithm.SHOW_STEPS) {
				System.out.println("extracting metrics");
				System.out.flush();
			}

			// extract metrics
			final double[] stats = gps.populationStats(population);
			bestFitness = stats[1];

			Chromosome<T> currentBestChromosome = population[population.length - 1];
			if (overallBestChromosome == null || currentBestChromosome.getFitness() > overallBestChromosome.getFitness()) {
				// do a copy because it may be modified
				overallBestChromosome = new Chromosome<>(currentBestChromosome);
			}

			String str = "epoch:\t" + currentGeneration + "\toverall best:" + overallBestChromosome.getFitness() + "\tcurrent best:\t" + bestFitness + "\tbest candidate:\t"
					+ Arrays.toString(overallBestChromosome.genes);
			System.out.println(str.replace(',', '.'));
			System.out.flush();

			/** [Loop] Go to step [New population] **/
			currentGeneration++;
		}
		es.shutdown();
		population[population.length - 1] = overallBestChromosome;
		System.out.println("done...");
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
		if (GeneticAlgorithm.SHOW_STEPS) {
			System.out.println("initializing population");
			System.out.flush();
		}

		// create population with random chromosomes using parallel tasks
		ArrayList<Callable<T>> tasks = new ArrayList<Callable<T>>();
		int rangeSize = (endI - startI) / amountThreads;
		int rangeL = startI;
		int rangeH = rangeL + rangeSize;
		for (int threadId = 0; threadId < amountThreads; threadId++) {
			Callable<T> call = new PopulationInitializerThread<T>(pop, rangeL, rangeH, numberOfGenes, threadRNGs.get(threadId), repairingOperator, geneInitializer, geneClass);
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
