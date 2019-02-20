package jcfgonc.patternminer.moea;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import jcfgonc.patternminer.PatternChromosome;
import jcfgonc.patternminer.PatternFinderUtils;

public class PatternMinerProblem extends AbstractProblem {

	public PatternMinerProblem() {
		super(1, PatternFinderUtils.numberOfObjectives);
	}

	@Override
	public Solution newSolution() {
		PatternChromosome pc = new PatternChromosome();
		pc.randomize();

		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());
		solution.setVariable(0, pc);
		return solution;
	}

	@Override
	public void evaluate(Solution solution) {
		PatternChromosome pc = (PatternChromosome) solution.getVariable(0);
		double[] objectives = pc.calculateObjectives();
		solution.setObjectives(objectives);
	}

}
