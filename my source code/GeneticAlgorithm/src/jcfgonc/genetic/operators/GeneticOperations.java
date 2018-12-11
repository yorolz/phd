package jcfgonc.genetic.operators;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.genetic.Chromosome;

/**
 * 
 * @author ck
 *
 * @param <T>
 */
public interface GeneticOperations<T> {

	/**
	 * Given the genes, returns a new copy of them. Should make a fully (as possible) deep copy of the original when possible (to prevent further errors).
	 * 
	 * @param genes
	 * @param soonChanged If true it means the genes are going to be mutated next. Used as an optimization flag.
	 * @return
	 */
	public T createGeneCopy(T genes, boolean soonChanged);

	/**
	 * Given two pre-initialized offspring chromosomes, crosses-over genes from parents into offspring. Use getGenes and setGenes on the chromosomes to manipulate them.
	 * 
	 * @param parent0
	 * @param parent1
	 * @param offSpring0
	 * @param offSpring1
	 */
	public void crossover(final T parent0, final T parent1, final T offSpring0, final T offSpring1, RandomGenerator random);

	/**
	 * Initializes the genes of the chromosome. Has to return the genes which are set in the chromosome.
	 * 
	 * @param genes
	 */
	public T initializeGenes(final RandomGenerator random);

	/**
	 * Given an array of type T genes, applies a random mutation to each gene (element of the array) or all of them. Use getGenes and setGenes on the chromosomes to manipulate
	 * them.
	 * 
	 * @param genes
	 */
	public void mutateGenes(final Chromosome<T> chromosome, RandomGenerator random);

	/**
	 * Given the T genes, apply repairing algorithm to them. Must return the genes repaired.
	 * 
	 * @param repaired genes
	 */
	public T repairGenes(final T genes, RandomGenerator random);

	/**
	 * if true, the genetic algorithm will invoke the function crossover()
	 * 
	 * @return
	 */
	public boolean useCrossover();

	/**
	 * if true, the genetic algorithm will invoke the function repairGenes()
	 * 
	 * @return
	 */
	public boolean useGeneRepair();

	/**
	 * if true, the genetic algorithm will invoke the function mutateGenes()
	 * 
	 * @return
	 */
	public boolean useMutation();

	/**
	 * Given the genes, it returns the fitness of the representing individual.
	 * 
	 * @param genes
	 * @return
	 */
	public double evaluateFitness(final T genes);

}
