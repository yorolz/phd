package jcfgonc.patternminer.moea;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import graph.StringGraph;
import jcfgonc.patternminer.PatternChromosome;

public class PatternMinerProblem extends AbstractProblem {

	private static final int NUM_OBJECTIVES = countObjectives();

	public PatternMinerProblem() {
		super(1, NUM_OBJECTIVES);
		System.out.format("initializing problem with %d objectives\n", NUM_OBJECTIVES);
	}

	@Override
	public Solution newSolution() {
		PatternChromosome pc = new PatternChromosome(new StringGraph());
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

	public static int countObjectives() {
		// lousy hack to get the number of objectives
		StringGraph pattern = new StringGraph();
		PatternChromosome pc = new PatternChromosome(pattern);
		double[] objectives = pc.calculateObjectives();
		int n = objectives.length;
		return n;
	}

}
