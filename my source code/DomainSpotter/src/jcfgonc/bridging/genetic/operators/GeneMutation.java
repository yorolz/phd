package jcfgonc.bridging.genetic.operators;

import org.apache.commons.math3.random.RandomGenerator;

public interface GeneMutation<T> {

	/**
	 * Given an array of type T genes, applies a random mutation to each gene (element of the array) or all of them.
	 * 
	 * @param genes
	 */
	public void mutateGenes(final T[] genes, RandomGenerator random);

}
