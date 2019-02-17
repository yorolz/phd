package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;
import org.moeaframework.core.Variable;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.ListOfSet;

public class PatternChromosome implements Variable {
	private static final long serialVersionUID = 1449562469642894508L;
	public StringGraph pattern;
	public double countingTime;
	public double matches;
	public double relationStd;
	public ListOfSet<String> components;
	public Object2IntOpenHashMap<String> relations;
	public int cycles;
	public StringGraph patternWithVars;
	public double fitness;

	public PatternChromosome(StringGraph pattern) {
		super();
		resetInternals();
		this.pattern = pattern;
	}

	public PatternChromosome(PatternChromosome other) {
		StringGraph otherPattern = other.pattern;
		this.pattern = new StringGraph(otherPattern);
		this.countingTime = other.countingTime;
		this.matches = other.matches;
		this.relationStd = other.relationStd;
		this.components = new ListOfSet<String>(other.components);
		this.relations = new Object2IntOpenHashMap<String>(other.relations);
		this.cycles = other.cycles;
		this.patternWithVars = new StringGraph(other.patternWithVars);
		this.fitness = other.fitness;
	}

	public PatternChromosome(StringGraph inputSpace, RandomGenerator random) {
		this(PatternFinderUtils.initializePattern(inputSpace, random));
	}

	public void resetInternals() {
		this.countingTime = 0;
		this.matches = 0;
		this.relationStd = 0;
		this.components = null;
		this.relations = null;
		this.cycles = 0;
		this.patternWithVars = null;
		this.fitness = 0;
	}

	@Override
	public PatternChromosome copy() {
		return new PatternChromosome(this);
	}

	@Override
	public void randomize() {
		System.out.println("randomize()");
	}

	@Override
	public String toString() {
		return "fitness\t" + fitness + //
				"\trelationTypes\t" + relations.size() + //
				"\trelationTypesStd\t" + relationStd + //
				"\tcycles\t" + cycles + //
				// "\tcomponents\t" + genes.components.size() + //
				"\tpattern edges\t" + pattern.numberOfEdges() + //
				"\tpattern vars\t" + pattern.numberOfVertices() + //
				"\ttime\t" + countingTime + //
				"\tmatches\t" + matches + //
				"\tpattern\t" + patternWithVars;
	}

	public double evaluateFitness(KnowledgeBase kb) {
		this.fitness = PatternFinderUtils.calculateFitness(this, kb);
		return fitness;
	}

	public double[] calculateObjectives(KnowledgeBase kb) {
		double[] objs = PatternFinderUtils.calculateObjectives(this, kb);
		return objs;
	}

}
