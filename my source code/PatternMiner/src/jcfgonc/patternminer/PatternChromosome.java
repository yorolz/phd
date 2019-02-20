package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;
import org.moeaframework.core.Variable;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.ListOfSet;

public class PatternChromosome implements Variable {
	public StringGraph pattern;
	public double countingTime;
	public double matches;
	public double relationStd;
	public ListOfSet<String> components;
	public Object2IntOpenHashMap<String> relations;
	public int cycles;
	public StringGraph patternWithVars;

	private static final long serialVersionUID = 1449562469642894508L;
	public static StringGraph kbGraph = null;
	public static RandomGenerator random = null;
	public static KnowledgeBase kb = null;

	public void resetInternals() {
		this.countingTime = 0;
		this.matches = 0;
		this.relationStd = 0;
		this.components = null;
		this.relations = null;
		this.cycles = 0;
		this.patternWithVars = null;
	}

	public PatternChromosome() {
		super();
		resetInternals();
	}

	public PatternChromosome(StringGraph pattern) {
		super();
		resetInternals();
		this.pattern = pattern;
		// this.components = new ListOfSet<>();
		// this.components.add(new HashSet<>());
	}

	public PatternChromosome(PatternChromosome other) {
		super();
		StringGraph otherPattern = other.pattern;
		this.pattern = new StringGraph(otherPattern);
		this.countingTime = other.countingTime;
		this.matches = other.matches;
		this.relationStd = other.relationStd;
		this.components = new ListOfSet<String>(other.components);
		this.relations = new Object2IntOpenHashMap<String>(other.relations);
		this.cycles = other.cycles;
		this.patternWithVars = new StringGraph(other.patternWithVars);
	}

	@Override
	public PatternChromosome copy() {
		return new PatternChromosome(this);
	}

	@Override
	public void randomize() {
		this.pattern = PatternFinderUtils.initializePattern(kbGraph, random);
		PatternFinderUtils.removeAdditionalComponents(this, null); // check for components (old repairing operator)
	}

	@Override
	public String toString() {
		return "time\t" + countingTime + //
				"\trelationTypes\t" + relations.size() + //
				"\trelationTypesStd\t" + relationStd + //
				"\tcycles\t" + cycles + //
				"\tcomponents\t" + components.size() + //
				"\tpattern edges\t" + pattern.numberOfEdges() + //
				"\tpattern vars\t" + pattern.numberOfVertices() + //
				"\tmatches\t" + matches + //
				"\tpattern\t" + patternWithVars;
	}

	public double[] calculateObjectives() {
		double[] objs = PatternFinderUtils.calculateObjectives(this, kb);
		return objs;
	}

	public void mutate() {
		PatternFinderUtils.mutatePattern(kbGraph, random, pattern, false); // do the mutation
		PatternFinderUtils.removeAdditionalComponents(this, null); // check for components (old repairing operator)
	}

}
