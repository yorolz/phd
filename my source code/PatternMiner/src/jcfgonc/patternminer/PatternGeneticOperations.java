package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.StringGraph;
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
	public PatternChromosome initializeGenes(RandomGenerator random) {
		StringGraph pattern = PatternFinderUtils.initializePattern(inputSpace, random);
		return new PatternChromosome(pattern);
	}

	@Override
	public PatternChromosome createGeneCopy(PatternChromosome genes, boolean soonChanged) {
		return new PatternChromosome(genes);
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
		PatternFinderUtils.removeAdditionalComponents(random, genes);
		return genes;
	}

	@Override
	public double evaluateFitness(PatternChromosome genes) {
		StringGraph pattern = genes.pattern;

		double fitness = PatternFinderUtils.calculateFitness(genes, kb);

		System.out.println("fitness\t" + fitness + //
				"\trelationTypes\t" + genes.relations.size() + //
				"\trelationTypesStd\t" + genes.relationStd + //
				// "\tcomponents\t" + genes.components.size() + //
				"\tpattern edges\t" + pattern.numberOfEdges() + //
				"\tpattern vars\t" + pattern.numberOfVertices() + //
				"\ttime\t" + genes.countingTime + //
				"\tmatches\t" + genes.matches + //
				"\tpattern\t" + genes.patternAsString);

		return fitness;
	}

//	@Override
//	public int getNumberOfObjectives() {
//		return 2;
//	}

//	@Override
//	public boolean aDominatesB(Chromosome<PatternChromosome> ca, Chromosome<PatternChromosome> cb) {
//		double[] x = ca.getFitness().getDataRef();
//		double[] y = cb.getFitness().getDataRef();
//		if (x[0] < y[0]) {
//			return false;
//		}
//		if (x[1] < y[1]) {
//			return false;
//		}
//		if()
//		return 0;
//	}
}
