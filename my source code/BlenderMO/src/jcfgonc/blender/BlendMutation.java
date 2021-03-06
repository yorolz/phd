package jcfgonc.blender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.blender.structures.Blend;
import jcfgonc.blender.structures.ConceptPair;
import jcfgonc.blender.structures.Mapping;

public class BlendMutation {
	public static void mutateBlend(RandomGenerator random, Blend blend, StringGraph inputSpace) {
		mutateEdges(random, blend, inputSpace);
		// TODO!!! test if any concept of a mapping pair is present separately in the blend
		// mutateMappings(random, blend); // mappings are currently static
	}

	private static void mutateEdges(RandomGenerator random, Blend blend, StringGraph inputSpace) {
		StringGraph blendSpace = blend.getBlendSpace();
		Mapping<String> mapping = blend.getMapping();
		int numEdges = blendSpace.numberOfEdges();

		if (numEdges == 0) { // no edge -> always add one edge
			addRandomEdge(random, blendSpace, inputSpace, mapping);

		} else if (numEdges == 1) { // one edge -> add another edge with the possibility of clearing the blend before

			if (random.nextBoolean()) { // remove the existing edge if god of random wishes so
				blendSpace.clear();
			}
			addRandomEdge(random, blendSpace, inputSpace, mapping);

		} else { // two or more edges -> add another edge OR remove one of the existing
			if (random.nextBoolean()) {
				addRandomEdge(random, blendSpace, inputSpace, mapping);

			} else {
				removeRandomEdge(random, blendSpace);
			}
		}
	}

	/**
	 * adds a random edge from the input space to the blend space using the existing blend's concept if possible <br>
	 * TESTED, SEEMS OK
	 * 
	 * @param random
	 * @param blendSpace
	 * @param inputSpace
	 * @param referenceConcept
	 * @return true if a new edge was added, false otherwise (because it could not)
	 */
	private static boolean addRandomNeighbourEdge(String referenceConcept, StringGraph blendSpace, StringGraph inputSpace, RandomGenerator random) {
		// get neighbor edges touching the concept in the input space
		// check if the concept is a blend or not
		if (referenceConcept.contains("|")) {// the concept is a blended concept, get touching edges from both components
			String[] concepts = referenceConcept.split("\\|");
			String c0 = concepts[0];
			String c1 = concepts[1];
			Set<StringEdge> e0 = inputSpace.edgesOf(c0);
			Set<StringEdge> e1 = inputSpace.edgesOf(c1);
			Set<StringEdge> edges = GraphAlgorithms.mergeSets(e0, e1);
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = GraphAlgorithms.subtract(edges, blendSpace.edgeSet(), true);
			if (newEdges == null || newEdges.isEmpty()) {
				// System.out.println("could not add a new edge from " + referenceConcept);
				return false;
				// throw new RuntimeException();
			}
			// get one of the touching edges
			StringEdge edgeToAdd = GraphAlgorithms.getRandomElementFromCollection(newEdges, random);

			// these two lines are to maintain the existence of the blended concept which does not exist in the input space
			edgeToAdd = edgeToAdd.replaceSourceOrTarget(c0, referenceConcept); // replace 'a' with 'a|b'
			edgeToAdd = edgeToAdd.replaceSourceOrTarget(c1, referenceConcept); // replace 'b' with 'a|b'

			// add the new edge
			blendSpace.addEdge(edgeToAdd);
		} else { // not a blended concept
			Set<StringEdge> edges = inputSpace.edgesOf(referenceConcept);
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = GraphAlgorithms.subtract(edges, blendSpace.edgeSet(), true);
			if (newEdges == null || newEdges.isEmpty()) {
				// System.out.println("could not add a new edge from " + referenceConcept);
				return false;
				// throw new RuntimeException();
			}
			// get one of the touching edges
			StringEdge edgeToAdd = GraphAlgorithms.getRandomElementFromCollection(newEdges, random);

			// add the new edge
			blendSpace.addEdge(edgeToAdd);
		}
		return true;
	}

	/**
	 * adds a random edge using a concept pair from the input space to the blend space
	 * 
	 * @param random
	 * @param blendSpace
	 * @param inputSpace
	 * @return
	 * @return true if a new edge was added, false otherwise (because it could not)
	 */
	private static boolean addRandomNeighbourEdgeUsingMapping(RandomGenerator random, StringGraph blendSpace, StringGraph inputSpace,
			Mapping<String> mapping) {
		Set<String> blendVertexSet = blendSpace.getVertexSet();
		RandomAdaptor randomAdapter = new RandomAdaptor(random);
		if (blendVertexSet.isEmpty()) { // if the blend space is empty, get an edge touching the left OR right concepts of a pair
			// this block seems OK
			ConceptPair<String> pair = mapping.getRandomPair(randomAdapter);
			// first get all edges touching the left and right concepts in the pair
			String left = pair.getLeftConcept();
			String right = pair.getRightConcept();
			Set<StringEdge> el = inputSpace.edgesOf(left);
			Set<StringEdge> er = inputSpace.edgesOf(right);
			Set<StringEdge> edges = GraphAlgorithms.mergeSets(el, er);
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = GraphAlgorithms.subtract(edges, blendSpace.edgeSet(), true);
			if (newEdges.isEmpty()) {
				throw new RuntimeException();
			}
			// add one of the touching edges, replacing left/right concepts with the blended pair
			String blendedPair = left + "|" + right;
			StringEdge edgeToAdd = GraphAlgorithms.getRandomElementFromCollection(newEdges, random);
			// replace original left/right concepts with the blended concept
			StringEdge edgeToAdd1 = edgeToAdd.replaceSourceOrTarget(left, blendedPair);
			StringEdge edgeToAdd2 = edgeToAdd1.replaceSourceOrTarget(right, blendedPair);
			// only one of the above two replacements is supposed to change the new edge
			if (!edgeToAdd.equals(edgeToAdd1) && !edgeToAdd1.equals(edgeToAdd2)) {
				System.out.flush();
				System.err.println("edgeToAdd: " + edgeToAdd);
				System.err.println("edgeToAdd1: " + edgeToAdd1);
				System.err.println("edgeToAdd2: " + edgeToAdd2);
				System.err.flush();
				throw new RuntimeException();
			}

			if (StringUtils.countMatches(edgeToAdd2.toString(), '|') == 2) {
				String str = "edge with two blended concepts: " + edgeToAdd2.toString();
				throw new RuntimeException(str);
			}
			blendSpace.addEdge(edgeToAdd2);
			return true;
		} else {
			// iterate *randomly* through the blend searching for insertable edges connecting one concept to a concept pair
			ArrayList<String> blendConcepts = new ArrayList<>(blendSpace.getVertexSet());
			Collections.shuffle(blendConcepts, randomAdapter);
			for (String concept : blendConcepts) { // try to add something
				if (concept.contains("|")) {
//TODO
					// String[] concepts = concept.split("\\|");
					// Set<StringEdge> edges = GraphAlgorithms.mergeSets(inputSpace.edgesOf(concepts[0]), inputSpace.edgesOf(concepts[1]));
					// // subtract from those edges the ones already existing in the blend space
					// Set<StringEdge> newEdges = GraphAlgorithms.subtract(edges, blendSpace.edgeSet());

				} else {
					// iterate *randomly* through each connected edge in the input space
					// subtract from those edges the ones already existing in the blend space
					Set<StringEdge> ec = inputSpace.edgesOf(concept);
					Set<StringEdge> edges = GraphAlgorithms.subtract(ec, blendSpace.edgeSet(), true);
					ArrayList<StringEdge> shuffledEdges = new ArrayList<>(edges);
					Collections.shuffle(shuffledEdges, randomAdapter);
					// see if the edge connects to a concept involved in the mapping
					for (StringEdge edge : shuffledEdges) {
						String otherConcept = edge.getOppositeOf(concept);
						if (mapping.containsConcept(otherConcept)) { // other concept is involved in the mapping
							// get the involved concept pair
							ConceptPair<String> conceptPair = mapping.getConceptPair(otherConcept);
							// recreate the edge renaming the other concept to a concept pair blend
							String blendedPair = conceptPair.getLeftConcept() + "|" + conceptPair.getRightConcept();
							StringEdge edgeToAdd = edge.replaceSourceOrTarget(otherConcept, blendedPair);
							blendSpace.addEdge(edgeToAdd);
							return true; // done, one edge added
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * returns a set of concept pairs whose pairs (in the mapping) contain at least one of the given concepts in the mask
	 * 
	 * @param mask    list of concepts to search in the mapping
	 * @param mapping a mapping containing concept pairs
	 * @return
	 */
	@SuppressWarnings("unused")
	private static HashSet<ConceptPair<String>> getConceptPairsContainingConcepts(Set<String> mask, Mapping<String> mapping) {
		HashSet<ConceptPair<String>> pairs = new HashSet<ConceptPair<String>>();
		for (String concept : mask) {
			// get the pair containing the concept
			ConceptPair<String> pair = mapping.getConceptPair(concept);
			if (pair != null) {
				pairs.add(pair);
			}
		}
		return pairs;
	}

	@SuppressWarnings("unused")
	private static void addRandomEdge(RandomGenerator random, StringGraph blendSpace, StringGraph inputSpace, Mapping<String> mapping) {
		boolean added = false;
		int tries = 0;
		// either add a random edge using the mapping or not
//		if (random.nextBoolean()) { // use the mapping
		addRandomNeighbourEdgeUsingMapping(random, blendSpace, inputSpace, mapping);
		if (true)
			return;
//		} else { // OR do not use the mapping
		for (;;) {
			// randomly try to add an edge, it may fail - if so, try again
			// get a random concept from the blend space (if not empty) or from the input space (otherwise)
			Set<String> blendVertexSet = blendSpace.getVertexSet();
			String referenceConcept;
			if (blendVertexSet.isEmpty()) {
				referenceConcept = GraphAlgorithms.getRandomElementFromCollection(inputSpace.getVertexSet(), random);
			} else {
				referenceConcept = GraphAlgorithms.getRandomElementFromCollection(blendVertexSet, random);
				// System.out.println("chosen " + referenceConcept + " from " + blendVertexSet);
			}
			added = addRandomNeighbourEdge(referenceConcept, blendSpace, inputSpace, random);
			if (added)
				break;
			if (tries >= 10)
				break;
			// another round
			tries++;
		}
		if (!added) {
			// System.out.println("failed to add an edge");
		}
//		}
	}

	/**
	 * removes randomly one edge from the lowest degree vertex within the blend space.
	 * 
	 * @param random
	 * @param blendSpace
	 */
	private static void removeRandomEdge(RandomGenerator random, StringGraph blendSpace) {
		// get the vertex with the lowest degree
		String lowestDegreeVertex = GraphAlgorithms.getLowestDegreeVertex(blendSpace.getVertexSet(), blendSpace);
		// remove a random edge from that vertex
		Set<StringEdge> edgesOfLowest = blendSpace.edgesOf(lowestDegreeVertex);
		StringEdge edgeToDelete = GraphAlgorithms.getRandomElementFromCollection(edgesOfLowest, random);
		blendSpace.removeEdge(edgeToDelete);
	}

}
