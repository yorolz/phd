package jcfgonc.bridging.genetic.operators;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.bridging.genetic.Chromosome;

public interface GeneCrossover<T> {

	/**
	 * given two pre-initialized offspring chromosomes, crosses-over genes from parents into offspring
	 * 
	 * @param parent0
	 * @param parent1
	 * @param offspring0
	 * @param offspring1
	 */
	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random);

}
