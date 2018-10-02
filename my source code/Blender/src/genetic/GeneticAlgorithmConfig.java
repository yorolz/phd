package genetic;

import utils.OSTools;

public class GeneticAlgorithmConfig {
	/**
	 * The probability of occurring a crossover for each newborn chromosome.
	 */
	public static final double CROSSOVER_PROBABILITY = 0.1;
	/**
	 * If true, the random generator calls are deterministic.
	 */
	public static final boolean DETERMINISTIC = false;
	/**
	 * Diversity threshold to trigger increase in genetic diversity.
	 */
	public static final double DIVERSITY_REQUIRED = 5e-4;
	/**
	 * Size of the evolution debug window
	 */
	public static final int EVOLUTION_WINDOW_SIZE = 512;
	/**
	 * The maximum amount of generations to reach by the Genetic Algorithm.
	 */
	public static final int MAXIMUM_GENERATIONS = 1 << 30;
	/**
	 * Change in mutation probability when adapting diversity.
	 */
	public static final double MUTATION_DELTA = 0.005;
	/**
	 * The probability of occurring a mutation in each newborn gene.
	 */
	public static final double MUTATION_PROBABILITY = 0.25;
	/**
	 * how many threads to parallelize the fitness evaluation function
	 */
	public static final int NUMBER_OF_THREADS = OSTools.getNumberOfCPUCores() + 0;
	/**
	 * The size of the Genetic's Algorithm population (constant trough it's execution).
	 */
	public static final int POPULATION_SIZE = 1 << 10;
	/**
	 * Change in tournament strongest probability when adapting diversity.
	 */
	public static final double TOURNAMENT_DELTA = 0.001;
	/**
	 * Number of individuals entering the tournament.
	 */
	public static final int TOURNAMENT_SIZE = 2;
	/**
	 * The probability of the strongest individual winning in each selection tournament.
	 */
	public static final double TOURNAMENT_STRONGEST_PROBABILITY = 0.95;
}
