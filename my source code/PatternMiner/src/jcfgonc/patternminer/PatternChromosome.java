package jcfgonc.patternminer;

import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.ListOfSet;

public class PatternChromosome {

	public StringGraph pattern;
	public double countingTime;
	public double matches;
	public String patternAsString;
	public double relationStd;
	public ListOfSet<String> components;
	public Object2IntOpenHashMap<String> relations;

	public PatternChromosome(StringGraph pattern) {
		super();
		this.pattern = pattern;
		this.countingTime = 0;
		this.matches = 0;
		this.patternAsString = "";
		this.relationStd = 0;
	}

	public PatternChromosome(PatternChromosome other) {
		StringGraph otherPattern = other.pattern;
		this.pattern = new StringGraph(otherPattern);
		this.countingTime = other.countingTime;
		this.matches = other.matches;
		this.patternAsString = other.patternAsString;
	}
}
