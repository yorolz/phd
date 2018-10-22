package jcfgonc.patternminer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.jcfgonc.KnowledgeBaseBuilder;

import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.Ticker;

public class PatternGeneticOperations implements GeneticOperations<PatternChromosome> {

	private StringGraph kb;
	private KnowledgeBase kbb;

	public PatternGeneticOperations(StringGraph graph) {
		this.kb = graph;

		// SWI-JPL specific
		// ObjectIndex<String> concepts = new ObjectIndex<>();
		// PatternFinderSwiProlog.createKnowledgeBase(kb, concepts);

		// build KB
		System.out.println("Creating KB...");
		Ticker t = new Ticker();
		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		for (StringEdge edge : graph.edgeSet()) {
			String predicate = edge.getLabel();
			String subject = edge.getSource();
			String object = edge.getTarget();
			kbb.addFact(predicate, subject, object);
		}
		this.kbb = kbb.build();
		System.out.println("KB creation took (s) " + t.getElapsedTime());
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
				PatternFinderUtils.mutatePattern(kb, random, pattern, true);
			}
			return new PatternChromosome(pattern);
		}
	}

	@Override
	public void mutateGenes(Chromosome<PatternChromosome> chromosome, RandomGenerator random) {
		StringGraph pattern = chromosome.getGenes().getPattern();
		PatternFinderUtils.mutatePattern(kb, random, pattern, false);
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
		final int solutionLimit = 1 << 25;
		StringGraph pattern = genes.getPattern();
		System.exit(0);
		double matches = PatternFinderSwiProlog.countPatternMatches(pattern, solutionLimit);
		double fitness = FastMath.log(2, matches) / 10.0 + pattern.numberOfEdges();
		return fitness;
	}

}
