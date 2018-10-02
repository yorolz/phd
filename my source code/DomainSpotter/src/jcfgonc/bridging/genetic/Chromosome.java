package jcfgonc.bridging.genetic;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import jcfgonc.bridging.genetic.operators.FitnessEvaluator;

/**
 * @author CK
 */

public final class Chromosome<T> implements Comparable<Chromosome<T>> {

	private double fitness;
	public final T[] genes;
	public final Class<T> geneclass;

	@SuppressWarnings("unchecked")
	public Chromosome(Class<T> geneclass, int numberGenes) {
		this.geneclass = geneclass;
		this.genes = (T[]) Array.newInstance(geneclass, numberGenes);
		this.fitness = 0;
	}

	public Chromosome(Chromosome<T> other) {
		this(other.geneclass, other.genes.length);
		copyGenes(other);
		// this should be up to date
		this.fitness = other.fitness;
	}

	@Override
	public int compareTo(Chromosome<T> o) {

		final double d1 = this.fitness;
		final double d2 = o.fitness;

		if (d1 < d2)
			return -1; // Neither val is NaN, thisVal is smaller
		if (d1 > d2)
			return 1; // Neither val is NaN, thisVal is larger

		// Cannot use doubleToRawLongBits because of possibility of NaNs.
		final long thisBits = Double.doubleToLongBits(d1);
		final long anotherBits = Double.doubleToLongBits(d2);

		return (thisBits == anotherBits ? 0
				: // Values are equal
				(thisBits < anotherBits ? -1
						: // (-0.0, 0.0) or (!NaN, NaN)
						1)); // (0.0, -0.0) or (NaN, !NaN)
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object oo) {
		if (!(oo instanceof Chromosome))
			return false;
		Chromosome<T> other = (Chromosome<T>) oo;
		if (this.hashCode() != other.hashCode())
			return false;

		return Arrays.equals(this.genes, other.genes);
	}

	public double getFitness() {
		return fitness;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(genes);
	}

	/**
	 * from http://stackoverflow.com/questions/19216136/java-generics-copy-constructor by Pshemo
	 * 
	 * @param item
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T createCopy(T item) {
		T copy = null;
		try {
			Class<?> clazz = item.getClass();
			Constructor<?> copyConstructor = clazz.getConstructor(clazz);

			copy = (T) copyConstructor.newInstance(item);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return copy;
	}

	public void copyGenes(Chromosome<T> other) {
		// chromosome gene must have a copy constructor!
		for (int i = 0; i < genes.length; i++) {
			genes[i] = createCopy(other.genes[i]);
		}
	}

	@Override
	public String toString() {
		return "[fitness=" + fitness + " genes=" + Arrays.toString(this.genes) + "]";
	}

	/**
	 * Updates this chromosome's fitness according to it's fenotype. Always call this function following a modification in this chromosome.
	 * 
	 */
	public void updateFitness(FitnessEvaluator<T> fe) {
		this.fitness = fe.evaluateFitness(genes);
	}
}
