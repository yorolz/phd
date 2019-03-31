package jcfgonc.patternminer.moea;

import java.math.BigInteger;
import java.util.HashSet;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
import jcfgonc.patternminer.PatternChromosome;
import jcfgonc.patternminer.PatternFinderUtils;
import structures.GlobalFileWriter;

public class PatternMinerProblem implements Problem, ProblemDescription {

	private final KnowledgeBase kb;
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
	@SuppressWarnings("deprecation")
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

	public PatternMinerProblem(KnowledgeBase kb) {
		this.kb = kb;
		this.numberOfVariables = 1;
		this.numberOfObjectives = 3;
		this.numberOfConstraints = 2;
		// write log's header
		GlobalFileWriter
				.writeLine("n:time\tn:relationTypes\tn:relationTypesStd\tn:cycles\tn:patternEdges\tn:patternVertices\tn:matches\ts:query\ts:pattern\ts:conceptVarMap\ts:hash");
	}

	@Override
	public Solution newSolution() {
		PatternChromosome pc = new PatternChromosome(new StringGraph());
		pc.randomize();

		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
		solution.setVariable(0, pc);
		return solution;
	}

	private static HashSet<BigInteger> patternIDs = new HashSet<>();

	@Override
	public void evaluate(Solution solution) {
		PatternChromosome pc = (PatternChromosome) solution.getVariable(0);

		// all these functions store their results in PatternChromosome pc
		PatternFinderUtils.calculateRelationHistogram(pc);
		PatternFinderUtils.countPatternMatchesBI(pc, kb);
		PatternFinderUtils.countCycles(pc);

		String pcStr = pc.toString();

		// prevent pattern duplicates (sort of)
		BigInteger id = new BigInteger(pc.pattern.accurateHashCode());
		synchronized (patternIDs) {
			if (!patternIDs.contains(id)) {
				patternIDs.add(id);
				// System.out.println(pcStr);
				GlobalFileWriter.writeLine(pcStr);
			} else {
				System.err.println("REPEATED PATTERN: " + id.toString(16));
			}
		}

		solution.setObjective(0, -pc.matches);
		solution.setObjective(1, -pc.cycles);
		solution.setObjective(2, -pc.relations.size());

		// ---constraints
		// matches
		if (pc.matches >= 3) {
			solution.setConstraint(0, 0);
		} else { // all OK
			solution.setConstraint(0, 1);
		}
		// variety on relations
		if (pc.relations.size() >= 2) { // that is, at least two types of relations
			solution.setConstraint(1, 0);
		} else { // all OK
			solution.setConstraint(1, 1);
		}
	}

	@Override
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
	public String getVariableDescription(int varid) {
		return "Pattern Semantic Graph";
	}

}
