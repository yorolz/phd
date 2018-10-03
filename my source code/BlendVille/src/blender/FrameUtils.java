package blender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Var;
import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import prolog.PatternFrameCombination;
import structures.MapOfSet;

public class FrameUtils {
	private static MapOfSet<String, String> namespaceToConcepts;
	private static MapOfSet<String, StringEdge> namespaceToEdges;
	@SuppressWarnings("unused")
	private static Map<String, String> conceptToNamespace;
	private static HashMap<String, HashMap<String, MutableInt>> namespaceToRelationCount;

	public static void initialize(StringGraph inputspace) {
		FrameUtils.namespaceToConcepts = GraphAlgorithms.createNameSpaceToConceptSet(inputspace);
		FrameUtils.namespaceToEdges = GraphAlgorithms.createNameSpaceToEdgeSet(inputspace);
		FrameUtils.conceptToNamespace = GraphAlgorithms.createConceptToNameSpaceMap(inputspace);
		Set<String> inputspaceNamespaces = FrameUtils.namespaceToConcepts.keySet();
		FrameUtils.namespaceToRelationCount = getRelationCounter(inputspaceNamespaces, inputspace);
	}

	public static HashMap<String, MutableInt> getRelationCounter(String namespace, StringGraph inputspace) {
		Set<StringEdge> edges = namespaceToEdges.get(namespace);
		HashMap<String, MutableInt> typeCounter = new HashMap<>();
		for (StringEdge edge : edges) {
			String label = edge.getLabel();
			MutableInt counter = typeCounter.get(label);
			if (counter == null) {
				counter = new MutableInt(0);
				typeCounter.put(label, counter);
			}
			counter.increment();
		}
		return typeCounter;
	}

	/**
	 * Counts the label of relations for the given inputspace
	 * 
	 * @param graph
	 * @return
	 */
	public static HashMap<String, MutableInt> getRelationCount(StringGraph graph) {
		Set<StringEdge> edges = graph.edgeSet();
		HashMap<String, MutableInt> typeCounter = new HashMap<>();
		for (StringEdge edge : edges) {
			String label = edge.getLabel();
			MutableInt counter = typeCounter.get(label);
			if (counter == null) {
				counter = new MutableInt(0);
				typeCounter.put(label, counter);
			}
			counter.increment();
		}
		return typeCounter;
	}

	public static HashMap<String, HashMap<String, MutableInt>> getRelationCounter(Collection<String> namespaces, StringGraph inputspace) {
		HashMap<String, HashMap<String, MutableInt>> namespaceToRelationCount = new HashMap<>();
		for (String namespace : namespaces) {
			HashMap<String, MutableInt> relationCount = getRelationCounter(namespace, inputspace);
			namespaceToRelationCount.put(namespace, relationCount);
		}
		return namespaceToRelationCount;
	}

	/**
	 * aframe - The blend contains identical structure (relations) from input 1.
	 * 
	 * Description: Every relation R that is present in domain 1 (A), should also be present in the blend, regardless of the projection of the argument concepts
	 * (e.g. the ability relation in "ability(bird, fly)" should be present in the blend as in "ability( horse, fly)"). Once again, there is the special case of
	 * actor/actee relation descriptions, which should also be projected (e.g. if using aframe in blending "basketball and trash disposal", "shooting" should be
	 * projected to the blend, as well as the relations from the basketball domain.
	 * 
	 * @return the ratio of relations (labels) from the IS (for the given domain) present in the blend
	 */
	public static double evaluateEdgeFrame(String domain, StringGraph blendspace) {
		// count number of relations of each type in the given domain
		HashMap<String, MutableInt> inputRelationCount = FrameUtils.namespaceToRelationCount.get(domain);
		HashMap<String, MutableInt> blendRelationCount = FrameUtils.getRelationCount(blendspace);
		int inputspaceCount = 0;
		int blendspaceCount = 0;
		// for each relation label in the inputspace
		for (String relation : inputRelationCount.keySet()) {
			inputspaceCount += inputRelationCount.get(relation).intValue();
			// search for relations of the given label in the blendspace
			if (blendRelationCount.containsKey(relation)) {
				blendspaceCount += blendRelationCount.get(relation).intValue();
			}
		}
		double ratio;
		if (blendspaceCount > inputspaceCount) {
			ratio = 1;
		} else {
			ratio = (double) blendspaceCount / inputspaceCount;
		}
		return ratio;
	}

	/**
	 * aprojection - The blend contains the same elements (concepts) of input 1. Description: Every concept from domain 1 (A) should be projected (unchanged) to
	 * the blend. For example, in "aprojection(horse)", every single concept of "horse" (legs, mouth, snout, mane, neigh, run, cargo, pet, etc.) should be
	 * present in the blend.
	 * 
	 * @return the ratio of concepts from the IS (for the given domain) present in the blend
	 */
	public static double evaluateConceptFrame(String domain, StringGraph blendspace) {
		// check how many concepts from the given domain/inputspace are present in the blend
		Set<String> inputSpaceConcepts = namespaceToConcepts.get(domain);
		Set<String> blendConcepts = blendspace.getVertexSet();

		int blendTotalConcepts = 0;
		for (String concept : inputSpaceConcepts) {
			if (blendConcepts.contains(concept)) {
				blendTotalConcepts++;
			}
		}
		int inputspaceTotalConcepts = inputSpaceConcepts.size();
		double nameSpaceRatio;
		if (blendTotalConcepts >= inputspaceTotalConcepts) {
			nameSpaceRatio = 1;
		} else {
			nameSpaceRatio = (double) blendTotalConcepts / inputspaceTotalConcepts;
		}
		return nameSpaceRatio;
	}

	/**
	 * Returns a list of individual scores for each matched frame
	 * 
	 * @param engine
	 * @param patternFrames
	 * @return
	 */
	public static DoubleArrayList evaluatePatternFrame(Prolog engine, Collection<PatternFrameCombination> patternFrames) {

		// stores one score per frame
		DoubleArrayList patternScores = new DoubleArrayList(patternFrames.size());
		// now test the pattern frames
		for (PatternFrameCombination patternFrameCombination : patternFrames) {
			double solvedClauses = 0;
			// first test the full frame/prolog clause (all predicates)
			Term fullFrame = patternFrameCombination.getClauseAtHighestLevel();
			SolveInfo result = engine.solve(fullFrame);
			double patternSolvingRatio = 0;
			if (result.isSuccess()) {
				patternSolvingRatio = 1;
			} else {
				// if failed to solve the full frame, test individual predicates and combinations
				List<Term> level1 = patternFrameCombination.getClausesAtLevel(1);
				for (Term term : level1) {
					result = engine.solve(term);
					if (result.isSuccess()) {
						solvedClauses++;
					} else {
					}
					// this must be always < 1.0, so that the full test is the only way to score 1
					// thats why there is a + 1 in the denominator
					patternSolvingRatio = solvedClauses / (level1.size() + 1);
				}
			}
			patternScores.add(patternSolvingRatio);
		}
		return patternScores;
	}

	/**
	 * new ability - An element has an ability relation not existent in any of the inputs
	 */
	public void evaluateNewAbility() {
	}

	/**
	 * Returns a list (per frame) of multiple (one ore more solutions) results (variables/bindings)
	 * 
	 * @param engine
	 * @param deltaFrames
	 * @return
	 * @throws NoSolutionException
	 * @throws NoMoreSolutionException
	 */
	public static ArrayList<ArrayList<HashMap<String, String>>> evaluateDeltaFrame(Prolog engine, ArrayList<Term> deltaFrames)
			throws NoSolutionException, NoMoreSolutionException {
		// there will be a list (per frame) of multiple (one ore more solutions) results (variables/bindings)
		ArrayList<ArrayList<HashMap<String, String>>> frameToSolutionToBindings = new ArrayList<>();
		// now test the pattern frames
		for (Term deltaFrame : deltaFrames) {
			ArrayList<HashMap<String, String>> solutionToBindings = new ArrayList<>();
			frameToSolutionToBindings.add(solutionToBindings);
			SolveInfo result = engine.solve(deltaFrame);
			// check for various solutions
			boolean hasResults = result.isSuccess();
			while (hasResults) {
				List<Var> bindingVars = result.getBindingVars();
				HashMap<String, String> bindings = new HashMap<>();
				solutionToBindings.add(bindings);
				// store binded vars for current solution
				for (Var var : bindingVars) {
					Term link = var.getLink();
					// Term term = var.getTerm();
					// String name = var.getName();
					String originalName = var.getOriginalName();
					bindings.put(originalName, link.toString());
				}

				// if there is another solution, check it
				if (result.hasOpenAlternatives()) {
					result = engine.solveNext();
					hasResults = result.isSuccess();
				} else {
					hasResults = false;
				}
			}
		}
		return frameToSolutionToBindings;
	}
}
