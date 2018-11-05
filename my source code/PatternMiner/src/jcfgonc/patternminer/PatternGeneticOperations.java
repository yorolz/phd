package jcfgonc.patternminer;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

public class PatternGeneticOperations implements GeneticOperations<PatternChromosome> {

	private StringGraph inputSpace;
	private KnowledgeBase kb;

	public PatternGeneticOperations(StringGraph graph, KnowledgeBase kb) {
		this.inputSpace = graph;
		this.kb = kb;

		// SWI-JPL specific
		// ObjectIndex<String> concepts = new ObjectIndex<>();
		// PatternFinderSwiProlog.createKnowledgeBase(kb, concepts);
	}

	@Override
	public PatternChromosome createGeneCopy(PatternChromosome genes, boolean soonChanged) {
		return new PatternChromosome(genes);
	}

	@Override
	public void crossover(PatternChromosome parent0, PatternChromosome parent1, PatternChromosome offSpring0, PatternChromosome offSpring1, RandomGenerator random) {
		// DO NOTHING
	}

	@Override
	public PatternChromosome initializeGenes(RandomGenerator random) {
		// {
		// final int minNewConceptsTrigger = 4;
		// final int minTotalConceptsTrigger = 16;
		// HashSet<String> mask = GraphAlgorithms.extractRandomPart(kb, minNewConceptsTrigger, minTotalConceptsTrigger, random);
		// StringGraph pattern = new StringGraph(kb, mask);
		// return new PatternChromosome(pattern);
		// }

		{
			StringGraph pattern = new StringGraph();
			// generate a graph pattern
			for (int i = 0; i < 3; i++) {
				PatternFinderUtils.mutatePattern(inputSpace, random, pattern, true);
			}
			return new PatternChromosome(pattern);
		}
	}

	@Override
	public void mutateGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		StringGraph pattern = chromosome.getGenes().getPattern();
		PatternFinderUtils.mutatePattern(inputSpace, random, pattern, false);
	}

	@Override
	public void repairGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		// DO NOTHING
	}

	@Override
	public boolean useCrossover() {
		return false;
	}

	@Override
	public boolean useGeneRepair() {
		return false;
	}

	@Override
	public boolean useMutation() {
		return true;
	}

	@Override
	public double evaluateFitness(PatternChromosome genes) {
		StringGraph pattern = genes.getPattern();
		double matches = 0;
		int isaCounter = 0;
		Set<StringEdge> edgeSet = pattern.edgeSet();
		for (StringEdge edge : edgeSet) {
			if (edge.getLabel().equals("isa"))
				isaCounter++;
		}
		double isaRatio = (double) isaCounter / edgeSet.size();
		if (isaRatio > 0.7) {
			matches = 0;
		} else {
			matches = PatternFinderUtils.countPatternMatches(pattern, kb);
		}
		double fitness = matches + pattern.numberOfEdges() * 0;
		return fitness;
	}

}
