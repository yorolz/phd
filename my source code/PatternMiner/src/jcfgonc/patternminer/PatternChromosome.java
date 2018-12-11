package jcfgonc.patternminer;

import java.math.BigInteger;

import graph.StringGraph;

public class PatternChromosome {

	public StringGraph pattern;
	public double countingTime;
	public BigInteger matches;
	public String patternAsString;

	public PatternChromosome(StringGraph pattern) {
		super();
		this.pattern = pattern;
		this.countingTime = 0;
		this.matches = BigInteger.ZERO;
		this.patternAsString = "";
	}

	public PatternChromosome(PatternChromosome other) {
		StringGraph otherPattern = other.getPattern();
		this.pattern = new StringGraph(otherPattern);
		this.countingTime = other.countingTime;
		this.matches = other.matches;
		this.patternAsString = other.patternAsString;
	}

	public StringGraph getPattern() {
		return pattern;
	}

	public void setPattern(StringGraph pattern) {
		this.pattern = pattern;
	}

}
