package genetic.operators.implementation;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.operators.GeneMutation;

public class SwapMutation<T> implements GeneMutation<T> {

	@Override
	public void mutateGenes(final T[] genes, RandomGenerator random) {
		double rand = random.nextDouble();
		final int size = genes.length;
		rand = Math.pow(rand, 2) * size + 1;
		if (rand > size)
			rand = size;
		for (int i = 0; i < rand; i++) {
			final int i0 = random.nextInt(size);
			int i1;
			do {
				i1 = random.nextInt(size);
			} while (i0 == i1);
			swap(genes, i0, i1);
		}
	}

	private void swap(final T[] list, final int i0, final int i1) {
		T temp = list[i0];
		list[i0] = list[i1];
		list[i1] = temp;
	}
}
