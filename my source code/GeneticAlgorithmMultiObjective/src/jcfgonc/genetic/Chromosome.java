package jcfgonc.genetic;

import org.apache.commons.math3.linear.ArrayRealVector;

import jcfgonc.genetic.operators.GeneticOperations;

/**
 * @author CK
 */

public final class Chromosome<T> implements Comparable<Chromosome<T>> {

	private ArrayRealVector fitness;
	private T genes;
	private int numberOfObjectives;

	public Chromosome(int numberOfObjectives) {
		this.genes = null;
		this.numberOfObjectives = numberOfObjectives;
		this.fitness = new ArrayRealVector(numberOfObjectives, -Double.MAX_VALUE);
	}

	public Chromosome(T otherGenes, int numberOfObjectives) {
		this.genes = otherGenes;
		this.numberOfObjectives = numberOfObjectives;
		this.fitness = new ArrayRealVector(numberOfObjectives, -Double.MAX_VALUE);
	}

	@Override
	// used by overall to current generation best chromosome comparison, tournament selection and population sorting
	public int compareTo(Chromosome<T> o) {
		return compareParetoObjective(this, o);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Chromosome))
			return false;
		@SuppressWarnings("unchecked")
		Chromosome<T> otherCast = (Chromosome<T>) other;
		if (this.hashCode() != otherCast.hashCode())
			return false;

		return this.genes.equals(otherCast.genes);
	}

	public ArrayRealVector getFitness() {
		return fitness;
	}

	@Override
	public int hashCode() {
		return genes.hashCode();
	}

	public T getGenes() {
		return genes;
	}

	public void setGenes(T genes) {
		this.genes = genes;
	}

	@Override
	public String toString() {
		return genes.toString();
	}

	/**
	 * Updates this chromosome's fitness according to it's fenotype. Always call this function following a modification in this chromosome.
	 *
	 */
	public void updateFitness(GeneticOperations<T> go) {
		this.fitness = new ArrayRealVector(go.evaluateFitness(genes));
	}

	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

	/**
	 * Returns -1 if solution 1 is better, returns 1 if solution 2 is better, 0 otherwise. Adapted from:
	 * https://github.com/chen0040/java-moea/blob/master/src/main/java/com/github/chen0040/moea/utils/InvertedCompareUtils.java
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	public static <T> int compareParetoObjective(Chromosome<T> solution1, Chromosome<T> solution2) {
		boolean dominate1 = false;
		boolean dominate2 = false;

		int objective_count = solution1.getNumberOfObjectives();
		for (int i = 0; i < objective_count; i++) {
			if (solution1.getCost(i) < solution2.getCost(i)) {
				dominate1 = true; // solution 2 is dominated

				if (dominate2) {
					return 0;
				}
			} else if (solution1.getCost(i) > solution2.getCost(i)) {
				dominate2 = true; // solution 1 is dominated

				if (dominate1) {
					return 0;
				}
			}
		}

		if (dominate1 == dominate2) {
			return 0;
		} else if (dominate1) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * Cost is set as the opposite of the individual's fitness.
	 * 
	 * @param i
	 * @return
	 */
	private double getCost(int i) {
		return -fitness.getEntry(i);
	}
}
