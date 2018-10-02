package genetic;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import genetic.operators.FitnessEvaluator;

/**
 * @author CK
 */

public final class Chromosome<T> implements Comparable<Chromosome<T>> {

	private double[] fitness;
	public final T[] genes;
	public final Class<T> geneclass;

	@SuppressWarnings("unchecked")
	public Chromosome(Class<T> geneclass, int numberGenes) {
		this.geneclass = geneclass;
		this.genes = (T[]) Array.newInstance(geneclass, numberGenes);
		this.fitness = null;
	}

	public Chromosome(Chromosome<T> other, boolean copyGenes) {
		this(other.geneclass, other.genes.length);
		if (copyGenes) {
			copyGenes(other);
		}
		// this should be up to date
		this.fitness = new double[other.fitness.length];
	}

	@Override
	// used by overall to current generation best chromosome comparison, tournament selection and population sorting
	public int compareTo(Chromosome<T> o) {

		double[] thisFitness = this.fitness;
		double[] otherFitness = o.fitness;
		
		return Double.compare(thisFitness[0], otherFitness[0]);

//		for (int i = 0; i < thisFitness.length; i++) {
//			int comp = Double.compare(thisFitness[i], otherFitness[i]);
//			if (comp == 0) {
//			} else {
//				return comp;
//			}
//		}
//		return 0;
	}

	// @Override
	// // used by overall to current generation best chromosome comparison, tournament selection and population sorting
	// public int compareTo(Chromosome<T> o) {
	//
	// double[] thisFitness = this.fitness;
	// double[] otherFitness = o.fitness;
	// double sum = 0;
	//
	// for (int i = 0; i < thisFitness.length; i++) {
	// double tv = thisFitness[i];
	// double ov = otherFitness[i];
	// double ratio = tv / ov;
	// sum += ratio;
	// }
	// sum = sum / (double) thisFitness.length;
	// int compare = Double.compare(sum, 0.0);
	// return compare;
	// }

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

	public double[] getFitness() {
		return fitness;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(genes);
	}

	/**
	 * from http://stackoverflow.com/questions/19216136/java-generics-copy-constructor by Pshemo don't worry about creating new genes
	 * (performance) - I've thought about it.
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
		return Arrays.toString(fitness);
	}

	/**
	 * Updates this chromosome's fitness according to it's fenotype. Always call this function following a modification in this chromosome.
	 *
	 */
	public void updateFitness(FitnessEvaluator<T> fe) {
		this.fitness = fe.evaluateFitness(genes);
	}
}
