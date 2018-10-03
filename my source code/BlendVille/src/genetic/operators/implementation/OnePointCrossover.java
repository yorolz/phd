package genetic.operators.implementation;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;

/**
 * Does a one point crossover from the parents to their offspring.
 **/
public class OnePointCrossover<T> implements GeneCrossover<T> {

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {
		final int pos = random.nextInt(parent0.genes.length);
		for (int i = 0; i < pos; i++) {
			offspring0.genes[i] = parent0.genes[i];
			offspring1.genes[i] = parent1.genes[i];
		}
		for (int i = pos; i < parent0.genes.length; i++) {
			offspring0.genes[i] = parent1.genes[i];
			offspring1.genes[i] = parent0.genes[i];
		}
	}
}
