package jcfgonc.bridging.genetic.operators.implementation;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.bridging.genetic.Chromosome;
import jcfgonc.bridging.genetic.operators.GeneCrossover;

/**
 * does a two point crossover from the parents to their offspring. Assumes offspringN is an gene copy of parentN
 **/
public class TwoPointCrossover<T> implements GeneCrossover<T> {

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {
		// choose both an initial and ending point
		final int length = parent0.genes.length;
		int i0 = random.nextInt(length);
		int i1;
		do {
			i1 = random.nextInt(length);
		} while (i1 == i0);
		int a = Math.min(i0, i1);
		int b = Math.max(i0, i1);
		// cross
		for (int i = a; i < b; i++) {
			offspring0.genes[i] = parent1.genes[i];
			offspring1.genes[i] = parent0.genes[i];
		}
		// copy
		for (int i = 0; i < a; i++) {
			offspring0.genes[i] = parent0.genes[i];
			offspring1.genes[i] = parent1.genes[i];
		}
		for (int i = b; i < length; i++) {
			offspring0.genes[i] = parent0.genes[i];
			offspring1.genes[i] = parent1.genes[i];
		}
	}
}
