package jcfgonc.genetic.operators.implementation;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;

/**
 * does a two point crossover from the parents to their offspring. Assumes offspringN is an gene copy of parentN
 **/
public class UniformCrossover<T> implements GeneCrossover<T> {

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {
		for (int i = 0; i < parent0.genes.length; i++) {
			// cross
			if (random.nextBoolean()) {
				offspring0.genes[i] = parent1.genes[i];
				offspring1.genes[i] = parent0.genes[i];
			} else // or copy
			{
				offspring0.genes[i] = parent0.genes[i];
				offspring1.genes[i] = parent1.genes[i];
			}
		}
	}
}
