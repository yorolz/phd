package jcfgonc.patternminer;

import graph.StringGraph;

public class PatternChromosome {

	private StringGraph pattern;

	public PatternChromosome(StringGraph pattern) {
		super();
		this.pattern = pattern;
	}

	public PatternChromosome(PatternChromosome other) {
		StringGraph otherPattern = other.getPattern();
		this.pattern = new StringGraph(otherPattern);
	}

	public StringGraph getPattern() {
		return pattern;
	}

	public void setPattern(StringGraph pattern) {
		this.pattern = pattern;
	}

}
