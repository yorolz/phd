package genetic.operators;

/**
 * @author CK
 */
public interface FitnessEvaluator<T> {

	/**
	 * Given an array of genes this function returns the corresponding fitness. The caller will maximize this function, therefore if you aim to reach its
	 * mininum, return its symmetrical.
	 *
	 * @param genes
	 * @return
	 */
	public double[] evaluateFitness(final T[] genes);

}
