package jcfgonc.patternminer;

import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.ListOfSet;

public class PatternChromosome {

	public StringGraph pattern;
	public double countingTime;
	public double matches;
	public double relationStd;
	public ListOfSet<String> components;
	public Object2IntOpenHashMap<String> relations;
	public int loops;
	public StringGraph patternWithVars;

	public PatternChromosome(StringGraph pattern) {
		super();
		resetInternals();
		this.pattern = pattern;
	}
	
	public void resetInternals() {
		this.countingTime = 0;
		this.matches = 0;
		this.relationStd = 0;
		this.components = null;
		this.relations = null;
		this.loops = 0;
		this.patternWithVars = null;		
	}

	public PatternChromosome(PatternChromosome other) {
		StringGraph otherPattern = other.pattern;
		this.pattern = new StringGraph(otherPattern);
		this.countingTime = other.countingTime;
		this.matches = other.matches;
		this.relationStd = other.relationStd;
		this.components = new ListOfSet<String>(other.components);
		this.relations = new Object2IntOpenHashMap<String>(other.relations);
		this.loops = other.loops;
		this.patternWithVars = new StringGraph(other.patternWithVars);
	}
}
