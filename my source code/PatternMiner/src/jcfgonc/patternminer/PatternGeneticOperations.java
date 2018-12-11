package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.GraphAlgorithms;
import graph.StringGraph;
import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.ListOfSet;

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
	public void mutateGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		StringGraph pattern = chromosome.getGenes().getPattern();
		PatternFinderUtils.mutatePattern(inputSpace, random, pattern, false);
	}

	@Override
	public boolean useGeneRepair() {
		return true;
	}

	@Override
	public PatternChromosome repairGenes(final PatternChromosome genes, RandomGenerator random) {
		StringGraph removeAdditionalComponents = PatternFinderUtils.removeAdditionalComponents(random, genes.getPattern());
		genes.setPattern(removeAdditionalComponents);
		return genes;
	}

	@Override
	public double evaluateFitness(PatternChromosome genes) {
		StringGraph pattern = genes.getPattern();

		double fitness = PatternFinderUtils.calculateFitness(genes, kb);

		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);
		System.out.println("fitness\t" + fitness + //
				"\tcomponents\t" + components.size() + //
				"\tpattern edges\t" + pattern.numberOfEdges() + //
				"\tpattern vars\t" + pattern.numberOfVertices() + //
				"\ttime\t" + genes.countingTime + //
				"\tmatches\t" + genes.matches.toString(10) + //
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
