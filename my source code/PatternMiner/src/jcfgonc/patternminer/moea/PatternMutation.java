package jcfgonc.patternminer.moea;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import jcfgonc.patternminer.PatternChromosome;

public class PatternMutation implements Variation {

	/**
	 * The probability of mutating each variable in a solution.
	 */
	private final double probability;

	/**
	 * Constructs a Pattern mutation operator.
	 * 
	 * @param probability the probability of occurring the mutation
	 */
	public PatternMutation(double probability) {
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
		PatternChromosome variable = (PatternChromosome) offspring0.getVariable(0);
		variable.mutate();

		Solution[] offspring = new Solution[] { offspring0 };
		return offspring;
	}

	/**
	 * This is a mutation working on a single pattern at a time.
	 */
	@Override
	public int getArity() {
		return 1;
	}

}
