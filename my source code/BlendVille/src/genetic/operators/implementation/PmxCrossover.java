package genetic.operators.implementation;

import java.util.HashMap;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;

/**
 * Partialy-mapped crossover (PMX) builds offspring by choosing a subsequence of a tour from one parent preserving the order and position of as many positions
 * as possible from the other parent.
 **/
public class PmxCrossover<T> implements GeneCrossover<T> {

	/**
	 * Using code from jMetal library under GNU Lesser General Public License, version 2.1 PMXCrossover.java Class representing a partially matched (PMX)
	 * crossover operator
	 * 
	 * @author Antonio J. Nebro
	 * @version 1.0
	 */
	public void crossover(Chromosome<T> parent0, Chromosome<T> parent1, final Chromosome<T> offspring0, final Chromosome<T> offspring1,
			RandomGenerator random) {
		T[] parent1Vector = parent0.genes;
		T[] parent2Vector = parent1.genes;
		T[] offspring1Vector = offspring0.genes;
		T[] offspring2Vector = offspring1.genes;

		final int permutationLength = parent1Vector.length;

		// choose both an initial and ending point for the swath
		int cuttingPoint1 = random.nextInt(permutationLength);
		int cuttingPoint2 = -1;
		do {
			cuttingPoint2 = random.nextInt(permutationLength);
		} while (cuttingPoint2 == cuttingPoint1);

		if (cuttingPoint1 > cuttingPoint2) {
			int temp = cuttingPoint2;
			cuttingPoint2 = cuttingPoint1;
			cuttingPoint1 = temp;
			// Chromosome<T> ptemp = parent0;
			// parent0 = parent1;
			// parent1 = ptemp;
		}

		final HashMap<T, T> replacement1 = new HashMap<>();
		final HashMap<T, T> replacement2 = new HashMap<>();

		// STEP 2: Get the subchains to interchange
		for (int i = 0; i < permutationLength; i++) {
			offspring1Vector[i] = null;
			offspring2Vector[i] = null;
		}

		// STEP 3: Interchange
		for (int i = cuttingPoint1; i <= cuttingPoint2; i++) {
			offspring1Vector[i] = parent2Vector[i];
			offspring2Vector[i] = parent1Vector[i];

			replacement1.put(parent2Vector[i], parent1Vector[i]);
			replacement2.put(parent1Vector[i], parent2Vector[i]);
		} // for

		// STEP 4: Repair offsprings
		for (int i = 0; i < permutationLength; i++) {
			if ((i >= cuttingPoint1) && (i <= cuttingPoint2))
				continue;

			T n1 = parent1Vector[i];
			T m1 = replacement1.get(n1);

			T n2 = parent2Vector[i];
			T m2 = replacement2.get(n2);

			while (m1 != null) {
				n1 = m1;
				m1 = replacement1.get(m1);
			} // while
			while (m2 != null) {
				n2 = m2;
				m2 = replacement2.get(m2);
			} // while
			offspring1Vector[i] = n1;
			offspring2Vector[i] = n2;
		} // for
	}
}
