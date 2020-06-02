package jcfgonc.blender.old;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.MapOfSet;

/**
 * this is the old utility class related to frames used in the first blender version
 * 
 * @author jcfgonc
 *
 */
public class FrameUtilsOld {
	private static MapOfSet<String, String> namespaceToConcepts;
	private static MapOfSet<String, StringEdge> namespaceToEdges;
	@SuppressWarnings("unused")
	private static Map<String, String> conceptToNamespace;
	private static HashMap<String, HashMap<String, MutableInt>> namespaceToRelationCount;

	public static void initialize(StringGraph inputspace) {
		FrameUtilsOld.namespaceToConcepts = GraphAlgorithms.createNameSpaceToConceptSet(inputspace);
		FrameUtilsOld.namespaceToEdges = GraphAlgorithms.createNameSpaceToEdgeSet(inputspace);
		FrameUtilsOld.conceptToNamespace = GraphAlgorithms.createConceptToNameSpaceMap(inputspace);
		Set<String> inputspaceNamespaces = FrameUtilsOld.namespaceToConcepts.keySet();
		FrameUtilsOld.namespaceToRelationCount = getRelationCounter(inputspaceNamespaces, inputspace);
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
	 * Description: Every relation R that is present in domain 1 (A), should also be present in the blend, regardless of the projection of the
	 * argument concepts (e.g. the ability relation in "ability(bird, fly)" should be present in the blend as in "ability( horse, fly)"). Once again,
	 * there is the special case of actor/actee relation descriptions, which should also be projected (e.g. if using aframe in blending "basketball
	 * and trash disposal", "shooting" should be projected to the blend, as well as the relations from the basketball domain.
	 * 
	 * @return the ratio of relations (labels) from the IS (for the given domain) present in the blend
	 */
	public static double evaluateEdgeFrame(String domain, StringGraph blendspace) {
		// count number of relations of each type in the given domain
		HashMap<String, MutableInt> inputRelationCount = FrameUtilsOld.namespaceToRelationCount.get(domain);
		HashMap<String, MutableInt> blendRelationCount = FrameUtilsOld.getRelationCount(blendspace);
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
	 * aprojection - The blend contains the same elements (concepts) of input 1. Description: Every concept from domain 1 (A) should be projected
	 * (unchanged) to the blend. For example, in "aprojection(horse)", every single concept of "horse" (legs, mouth, snout, mane, neigh, run, cargo,
	 * pet, etc.) should be present in the blend.
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

}
