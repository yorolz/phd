package genetic.operators;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * 
 * @author ck
 *
 * @param <T>
 */
public interface GeneInitializer<T> {

	/**
	 * given an array of genes, allows a custom initialization for each chromosome
	 * 
	 * @param genes
	 */
	public void initializeGenes(final T[] genes, RandomGenerator random);

}
