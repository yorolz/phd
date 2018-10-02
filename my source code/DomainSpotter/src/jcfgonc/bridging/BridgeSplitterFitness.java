package jcfgonc.bridging;

import graph.StringGraph;
import jcfgonc.bridging.genetic.operators.FitnessEvaluator;

public class BridgeSplitterFitness implements FitnessEvaluator<BridgingGene> {

	@SuppressWarnings("unused")
	private StringGraph graph;

	public BridgeSplitterFitness(StringGraph graph) {
		this.graph = graph;
	}

	@Override
	public double evaluateFitness(BridgingGene[] genes) {
		BridgingGene bag = genes[0];
		return bag.getFitness();
	}
}
