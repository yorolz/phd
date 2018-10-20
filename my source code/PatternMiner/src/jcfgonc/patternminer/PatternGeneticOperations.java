package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;

import graph.StringGraph;
import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

public class PatternGeneticOperations implements GeneticOperations<PatternChromosome> {

	private StringGraph kb;

	public PatternGeneticOperations(StringGraph graph) {
		this.kb = graph;
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
				PatternFinderSwiProlog.mutatePattern(kb, random, pattern, true);
			}
			return new PatternChromosome(pattern);
		}
	}

	@Override
	public void mutateGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		StringGraph pattern = chromosome.getGenes().getPattern();
		PatternFinderSwiProlog.mutatePattern(kb, random, pattern, false);
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
		final int solutionLimit = 10000000;
		StringGraph pattern = genes.getPattern();
		return PatternFinderSwiProlog.countPatternMatches(pattern, solutionLimit);
	}

}
