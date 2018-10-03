package genetic.operators.implementation;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;

import java.lang.reflect.Constructor;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Passthrough crossover, ie, parent[i] genes copied into offspring[i] genes.
 * 
 * @author ck
 *
 * @param <T>
 */
public class PassthroughCrossover<T> implements GeneCrossover<T> {

	@SuppressWarnings("unchecked")
	public T createCopy(T item) {
		T copy = null;
		try {
			Class<?> clazz = item.getClass();
			Constructor<?> copyConstructor = clazz.getConstructor(clazz);

			copy = (T) copyConstructor.newInstance(item);
			// chromosome gene must have a copy constructor!
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return copy;
	}

	public void crossover(final Chromosome<T> parent0, final Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {
		System.out.println("PassthroughCrossover.crossover()");
		for (int i = 0; i < parent0.genes.length; i++) {
			offspring0.genes[i] = createCopy(parent0.genes[i]);
			offspring1.genes[i] = createCopy(parent1.genes[i]);
		}
	}
}
