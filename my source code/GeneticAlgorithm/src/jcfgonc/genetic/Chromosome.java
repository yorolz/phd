package jcfgonc.genetic;

import jcfgonc.genetic.operators.GeneticOperations;

/**
 * @author CK
 */

public final class Chromosome<T> implements Comparable<Chromosome<T>> {

	private double fitness;
	private T genes;

	public Chromosome() {
		this.genes = null;
		this.fitness = -Integer.MAX_VALUE;
	}

	public Chromosome(T otherGenes) {
		this.genes = otherGenes;
		this.fitness = -Integer.MAX_VALUE;
	}

	@Override
	// used by overall to current generation best chromosome comparison, tournament selection and population sorting
	public int compareTo(Chromosome<T> o) {

		double thisFitness = this.fitness;
		double otherFitness = o.fitness;

		return Double.compare(thisFitness, otherFitness);
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

	public double getFitness() {
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
		this.fitness = go.evaluateFitness(genes);
	}
}
