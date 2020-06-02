package jcfgonc.moea.specific;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import jcfgonc.moea.ProblemDescription;

public class CustomProblem implements Problem, ProblemDescription {
	/**
	 * {@code true} if the {@code close()} method has been invoked; {@code 
	 * false} otherwise.
	 */
	private boolean isClosed;
	/**
	 * The number of variables defined by this problem.
	 */
	protected final int numberOfVariables;

	/**
	 * The number of objectives defined by this problem.
	 */
	protected final int numberOfObjectives;

	/**
	 * The number of constraints defined by this problem.
	 */
	protected final int numberOfConstraints;

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int getNumberOfVariables() {
		return numberOfVariables;
	}

	@Override
	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

	@Override
	public int getNumberOfConstraints() {
		return numberOfConstraints;
	}

	/**
	 * Calls {@code close()} if this problem has not yet been closed prior to finalization.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (!isClosed) {
			close();
		}

		super.finalize();
	}

	@Override
	public void close() {
		isClosed = true;
	}

	// TODO define internal variables and MO setup here
	public CustomProblem() {
		this.numberOfVariables = 1;
		this.numberOfObjectives = 3;
		this.numberOfConstraints = 0;
	}

	@Override
	public Solution newSolution() {
		// TODO create a new solution here (used when starting the process)
		CustomChromosome pc = new CustomChromosome();
		pc.randomize();

		// do not touch this
		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
		solution.setVariable(0, pc); // unless the solution domain X has more than one dimension
		return solution;
	}

	@Override
	public void evaluate(Solution solution) {
		@SuppressWarnings("unused")
		CustomChromosome pc = (CustomChromosome) solution.getVariable(0); // unless the solution domain X has more than one dimension

		// all these functions store their results in PatternChromosome pc
//		PatternFinderUtils.calculateRelationHistogram(pc);
//		PatternFinderUtils.countPatternMatchesBI(pc, kb);
//		PatternFinderUtils.countCycles(pc);

		// TODO: set solution's objectives here
//		solution.setObjective(0, -pc.matches);
//		solution.setObjective(1, -pc.cycles);
//		solution.setObjective(2, -pc.relations.size());

		// TODO: set constraints if required here
//		if (pc.matches >= 3) {
//			solution.setConstraint(0, 0); // set to 0 if not OK
//		} else { // all OK
//			solution.setConstraint(0, 1);
//		}
//		if (pc.relations.size() >= 2) {
//			solution.setConstraint(1, 0); // set to 0 if not OK
//		} else { // all OK
//			solution.setConstraint(1, 1);// set to 1 if OK
//		}
	}

	@Override
	// TODO: this is used by the GUI, change these descriptions according to the corresponding objective
	public String getObjectiveDescription(int varid) {
		switch (varid) {
		case 0:
			return "Matches";
		case 1:
			return "Cycles";
		case 2:
			return "Types of Relations";
		}
		return null;
	}

	@Override
	// TODO: this is used by the GUI, change these descriptions according to the corresponding constraint
	public String getConstraintDescription(int varid) {
		switch (varid) {
		case 0:
			return "Matches >= 3";
		case 1:
			return "Types of Relations >= 2";
		}
		return null;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	// TODO: this is used by the GUI, change this description according to the solution domain (vector X)
	public String getVariableDescription(int varid) {
		return "Pattern Semantic Graph";
	}

}
