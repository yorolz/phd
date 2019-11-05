package jcfgonc.bridging.genetic;

import utils.OSTools;

public class GeneticAlgorithmConfig {
	/**
	 * The maximum amount of generations to reach by the Genetic Algorithm.
	 */
	public static final int GA_MAXIMUM_GENERATIONS = 1024;
	/**
	 * The probability of occurring a crossover for each newborn chromosome.
	 */
	public static final double GA_CROSSOVER_PROBABILITY = 0.00;
	/**
	 * The probability of occurring a mutation in each newborn gene.
	 */
	public static final double GA_MUTATION_PROBABILITY = 1.000;
	/**
	 * how many threads to parallelize the fitness evaluation function
	 */
	public static final int GA_NUMBER_OF_THREADS = OSTools.getCoreCount();
	/**
	 * The size of the Genetic's Algorithm population (constant trough it's execution).
	 */
	public static final int GA_POPULATION_SIZE = 256;
	/**
	 * the amount of chromosomes to be replaced in case the evolution stagnates
	 */
	public static final double GA_RANDOM_GENERATION_PERCENTAGE = 1.0;
	/**
	 * The probability of the weakest individual winning in each selection tournament.
	 */
	public static final double GA_TOURNAMENT_WEAKEST_PROBABILITY = 0.15;
	/**
	 * maximum allowed intersection between the two input spaces (excluding the bridge concept)
	 */
	public static final double MAX_INTERSECTION_RATIO = 0.25; // tau on paper
	public static final double MUTATION_JUMP_PROBABILITY_POWER = 2; // gamma on paper
	public static final int BRIDGE_JUMPING_RANGE = 3;
	public static final double NEARBY_BRIDGE_JUMP_PROB = 0.75;
}
