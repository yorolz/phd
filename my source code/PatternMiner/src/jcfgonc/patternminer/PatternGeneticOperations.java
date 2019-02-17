package jcfgonc.patternminer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
import jcfgonc.genetic.operators.GeneticOperations;

public class PatternGeneticOperations implements GeneticOperations<PatternChromosome> {

	private StringGraph inputSpace;
	private KnowledgeBase kb;
	private static BufferedWriter debugWriter;

	static {
		try {
			debugWriter = new BufferedWriter(new FileWriter("ga_console.txt"));
		} catch (IOException e) {
		}
	}

	public PatternGeneticOperations(StringGraph graph, KnowledgeBase kb) {
		this.inputSpace = graph;
		this.kb = kb;

		// SWI-JPL specific
		// ObjectIndex<String> concepts = new ObjectIndex<>();
		// PatternFinderSwiProlog.createKnowledgeBase(kb, concepts);
	}

	@Override
	public PatternChromosome initializeGenes(RandomGenerator random) {
		return new PatternChromosome(inputSpace, random);
	}

	@Override
	public PatternChromosome createGeneCopy(PatternChromosome genes, boolean soonChanged) {
		return genes.copy();
	}

	@Override
	public boolean useCrossover() {
		return false;
	}

	@Override
	public void crossover(PatternChromosome parent0, PatternChromosome parent1, PatternChromosome offSpring0, PatternChromosome offSpring1, RandomGenerator random) {
	}

	@Override
	public boolean useMutation() {
		return true;
	}

	@Override
	public PatternChromosome mutateGenes(PatternChromosome genes, RandomGenerator random) {
		StringGraph pattern = genes.pattern;
		PatternFinderUtils.mutatePattern(inputSpace, random, pattern, false);

		if (pattern.isEmpty()) {
			genes.pattern = PatternFinderUtils.initializePattern(inputSpace, random);
		}

		return genes;
	}

	@Override
	public boolean useGeneRepair() {
		return true;
	}

	@Override
	public PatternChromosome repairGenes(final PatternChromosome genes, RandomGenerator random) {
		PatternFinderUtils.removeAdditionalComponents(genes, null);
		return genes;
	}

	@Override
	public double evaluateFitness(PatternChromosome genes) {
		return genes.evaluateFitness(kb);
	}
}
