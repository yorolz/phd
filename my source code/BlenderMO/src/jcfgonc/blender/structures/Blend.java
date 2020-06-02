package jcfgonc.blender.structures;

import org.apache.commons.math3.random.RandomGenerator;

import graph.StringGraph;

public class Blend {
	private Mapping<String> mapping;
	private StringGraph blendSpace;
	private RandomGenerator randomGenerator;
	// private HashMap<String, String> scoreMap;

	public Blend(RandomGenerator randomGenerator_, Mapping<String> mapping_) {
		this.randomGenerator = randomGenerator_;
		this.mapping = mapping_;
		this.blendSpace = new StringGraph();
	}

	public StringGraph getBlendSpace() {
		return blendSpace;
	}

	public void setBlendSpace(StringGraph blendSpace) {
		this.blendSpace = blendSpace;
	}

	public Mapping<String> getMapping() {
		return mapping;
	}
}
