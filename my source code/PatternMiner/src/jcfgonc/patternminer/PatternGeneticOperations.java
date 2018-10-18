package jcfgonc.patternminer;
import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
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
	public void crossover(PatternChromosome parent0, PatternChromosome parent1, PatternChromosome offSpring0,
			PatternChromosome offSpring1, RandomGenerator random) {
		// DO NOTHING
	}

	@Override
	public void initializeGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		final int minNewConceptsTrigger = 4;
		final int minTotalConceptsTrigger = 16;
		GraphAlgorithms.extractRandomPart(kb, minNewConceptsTrigger, minTotalConceptsTrigger, random);
	}

	@Override
	public void mutateGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		StringGraph pattern = chromosome.getGenes().getPattern();
		PatternFinderSwiProlog.mutatePattern(kb, random, pattern);
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
		return PatternFinderSwiProlog.countPatternMatches(pattern);
	}

}
