package jcfgonc.genetic.operators.implementation;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;

/**
 * Adapted from CycleCrossoverFunction.java by Jeffrey Finkelstein (jmona).
 * 
 **/
public class CycleCrossover<T> implements GeneCrossover<T> {

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {

		// get the size of the tours
		final int size = parent0.genes.length;
		final ArrayList<Integer> cycleIndices = new ArrayList<Integer>();

		// choose a random initial index of a city in the tour
		int tour1index = random.nextInt(size - 1);

		// add that index to the cycle indices
		cycleIndices.add(tour1index);

		// get the city in tour2 at that index
		T tour2city = parent1.genes[tour1index];

		// get the index of that city in tour1
		tour1index = ArrayUtils.indexOf(parent0.genes, tour2city);

		// if tour1index = initial index, stop
		while (tour1index != cycleIndices.get(0)) {

			// add that index to the cycle indices
			cycleIndices.add(tour1index);

			// get the city in tour2 at that index
			tour2city = parent1.genes[tour1index];

			// get the index of that city in tour1
			tour1index = ArrayUtils.indexOf(parent0.genes, tour2city);
		}

		// swap the cities at each of the indices of the determined cycle
		for (int i = 0; i < size; i++) {
			if (cycleIndices.contains(i)) {
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
