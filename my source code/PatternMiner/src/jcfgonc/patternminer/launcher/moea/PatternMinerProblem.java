package jcfgonc.patternminer.launcher.moea;

import org.apache.commons.math3.random.RandomGenerator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
import jcfgonc.patternminer.PatternChromosome;

public class PatternMinerProblem extends AbstractProblem {

	private StringGraph inputSpace;
	private RandomGenerator random;
	private KnowledgeBase kb;

	public PatternMinerProblem() {
		super(1, 2);
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());
		solution.setVariable(0, new PatternChromosome(inputSpace, random));
		return solution;
	}

	@Override
	public void evaluate(Solution solution) {
		PatternChromosome pc = (PatternChromosome) solution.getVariable(0);
		double[] objectives = pc.calculateObjectives(kb);
		solution.setObjectives(objectives);
	}

}
