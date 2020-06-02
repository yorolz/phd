package jcfgonc.moea.specific;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class CustomMutation implements Variation {

	/**
	 * The probability of mutating each variable in a solution.
	 */
	private final double probability;

	/**
	 * Constructs a Pattern mutation operator.
	 * 
	 * @param probability the probability of occurring the mutation
	 */
	public CustomMutation(double probability) {
		super();
		this.probability = probability;
	}

	/**
	 * Returns the probability of occurring the mutation.
	 * 
	 * @return
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * returns a new offspring created as a mutated copy of a parent
	 */
	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution offspring0 = parents[0].copy();
		CustomChromosome variable = (CustomChromosome) offspring0.getVariable(0);
		variable.mutate();

		Solution[] offspring = new Solution[] { offspring0 };
		return offspring;
	}

	/**
	 * This is a mutation requiring one parent in the evolve() function.
	 */
	@Override
	public int getArity() {
		return 1;
	}

}
