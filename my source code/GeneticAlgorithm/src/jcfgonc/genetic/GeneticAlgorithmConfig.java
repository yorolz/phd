package jcfgonc.genetic;

import utils.OSTools;

@SuppressWarnings("unused")
public class GeneticAlgorithmConfig {
	/**
	 * The probability of occurring a crossover for each newborn chromosome.
	 */
	public static double CROSSOVER_PROBABILITY = 0.0;
	/**
	 * If true, the random generator calls are deterministic.
	 */
	public static final boolean DETERMINISTIC = false;
	/**
	 * Size of the evolution debug window
	 */
	public static final int EVOLUTION_WINDOW_SIZE = 512;
	/**
	 * The maximum amount of generations to reach by the Genetic Algorithm.
	 */
	public static int MAXIMUM_GENERATIONS = Integer.MAX_VALUE;
	/**
	 * The maximum amount of time taken by the algorithm, in seconds.
	 */
	public static double MAXIMUM_TIME_SECONDS = Double.MAX_VALUE; // 6 * 60 * 60;
	/**
	 * The probability of occurring a mutation in each newborn gene.
	 */
	public static double MUTATION_PROBABILITY = 1.00;
	/**
	 * how many threads parallelizing the fitness evaluation function
	 */
	public static final int NTHREADS_FITNESS = 16;// (int) (OSTools.getNumberOfCPUCores() * 3.0 / 2.0);
	/**
	 * how many threads parallelizing the genetic operations
	 */
	public static final int NTHREADS_GOPERATIONS = 16;// (int) (OSTools.getNumberOfCPUCores() * 3.0 / 2.0);
	/**
	 * The size of the Genetic's Algorithm population (constant trough it's execution).
	 */
	public static int POPULATION_SIZE = 2048;
	/**
	 * Number of individuals entering the tournament.
	 */
	public static int TOURNAMENT_SIZE = 2;
	/**
	 * The probability of the strongest individual winning in each selection tournament.
	 */
	public static double TOURNAMENT_STRONGEST_PROBABILITY = 0.75;
}
