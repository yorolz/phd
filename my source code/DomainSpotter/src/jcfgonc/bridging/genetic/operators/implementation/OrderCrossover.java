package jcfgonc.bridging.genetic.operators.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.bridging.genetic.Chromosome;
import jcfgonc.bridging.genetic.operators.GeneCrossover;

/**
 * Adapted from OrderedCrossoverFunction.java by Jeffrey Finkelstein (jmona).
 * 
 **/
public class OrderCrossover<T> implements GeneCrossover<T> {

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {

		// get the size of the tours
		final int size = parent0.genes.length;

		// choose two random numbers for the start and end indices of the slice
		// (one can be at index "size")
		// choose both an initial and ending point for the swath
		final int number1 = random.nextInt(size - 1);
		final int number2 = random.nextInt(size);

		// make the smaller the start and the larger the end
		final int start = Math.min(number1, number2);
		final int end = Math.max(number1, number2);

		// instantiate two child tours
		final List<T> child1 = new ArrayList<T>();
		final List<T> child2 = new ArrayList<T>();

		// add the sublist in between the start and end points to the children
		copyFromArrayIntoList(child1, parent0.genes, start, end);
		copyFromArrayIntoList(child2, parent1.genes, start, end);

		// iterate over each city in the parent tours
		int currentCityIndex = 0;
		T currentCityInTour1;
		T currentCityInTour2;
		for (int i = 0; i < size; i++) {

			// get the index of the current city
			currentCityIndex = (end + i) % size;

			// get the city at the current index in each of the two parent tours
			currentCityInTour1 = parent0.genes[currentCityIndex];
			currentCityInTour2 = parent1.genes[currentCityIndex];

			// if child 1 does not already contain the current city in tour 2, add it
			// Note: MutableIntegers override equals() so .contains() works
			if (!child1.contains(currentCityInTour2)) {
				child1.add(currentCityInTour2);
			}

			// if child 2 does not already contain the current city in tour 1, add it
			// Note: MutableIntegers override equals() so .contains() works
			if (!child2.contains(currentCityInTour1)) {
				child2.add(currentCityInTour1);
			}
		}

		// rotate the lists so the original slice is in the same place as in the
		// parent tours
		Collections.rotate(child1, start);
		Collections.rotate(child2, start);

		copyFromListIntoArray(child1, offspring0.genes);
		copyFromListIntoArray(child2, offspring1.genes);
	}

	private void copyFromListIntoArray(List<T> list, T[] array) {
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
	}

	private void copyFromArrayIntoList(List<T> list, T[] array, int start, int end) {
		for (int i = start; i < end; i++) {
			list.add(array[i]);
		}
	}
}
