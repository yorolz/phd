package blender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import blender.structures.AnalogySet;
import blender.structures.Mapping;
import genetic.operators.GeneMutation;
import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;

public class BlendMutation implements GeneMutation<Blend> {
	private static double analogyResetProbability = 1e-9;
	private static int MAX_EDGES_TO_ADD = 4;
	private static int MAX_EDGES_TO_REMOVE = 4;

	private void mutateEdges(RandomGenerator random, Blend blend) {
		StringGraph inputSpace = Blend.getInputSpace();
		StringGraph outputSpace = blend.getOutputSpace();
		Set<Mapping<String>> mappings = blend.getMappings();

		// remove edges from the blend/output space
		{
			Set<StringEdge> blendSpaceEdgeSet = outputSpace.edgeSet();
			int numberEdgesToDelete = (int) (GraphAlgorithms.getRandomDoublePow(random, 2.0) * MAX_EDGES_TO_REMOVE);
			for (int i = 0; i < numberEdgesToDelete; i++) {
				if (blendSpaceEdgeSet.size() == 0)
					break;
				StringEdge edgeToDelete = GraphAlgorithms.getRandomElementFromCollection(blendSpaceEdgeSet, random);
				outputSpace.removeEdge(edgeToDelete);
			}
		}

		// add edges to the blend/output space from the input space
		{
			Set<StringEdge> intputSpaceEdgeSet = inputSpace.edgeSet();
			int numberEdgesToAdd = (int) (GraphAlgorithms.getRandomDoublePow(random, 2.0) * MAX_EDGES_TO_ADD);
			for (int i = 0; i < numberEdgesToAdd; i++) {
				StringEdge edgeToAdd = GraphAlgorithms.getRandomElementFromCollection(intputSpaceEdgeSet, random);
				outputSpace.addEdge(edgeToAdd);
			}
		}

		// add one or more blended concepts and nearby edges
		{
			for (Mapping<String> currentMapping : mappings) {
				// use that mapping?
				if (random.nextBoolean())
					continue;
				// bring edges from either left or right original concepts
				for (String oldConcept : currentMapping.getConcepts()) {
					Set<StringEdge> edgesOf = inputSpace.edgesOf(oldConcept);
					// generate a random amount of new edges containing the new concept
					int n = edgesOf.size();
					if (n > 0) {
						double numberEdgesToAdd = (double) n * random.nextDouble() * 0.25;
						Set<StringEdge> newEdgesOf = GraphAlgorithms.randomSubSet(edgesOf, (int) numberEdgesToAdd, random);
						for (StringEdge edge : newEdgesOf) {
							// bring the edge from inputspace and replace old concept with blend OR opposing concept
							String newConcept;
							// blend?
							if (random.nextBoolean()) {
								newConcept = currentMapping.getLeftConcept() + "|" + currentMapping.getRightConcept();
							} else {
								// opposing concept
								newConcept = currentMapping.getOpposingConcept(oldConcept);
							}
							outputSpace.replaceEdgeSourceOrTarget(edge, oldConcept, newConcept);
						}
					}
				}
			}
		}
		// mutate an existing concept to a blend or opposing concept
		{
			for (Mapping<String> currentMapping : mappings) {
				String leftConcept = currentMapping.getLeftConcept();
				String rightConcept = currentMapping.getRightConcept();
				String blendedConcept = leftConcept + "|" + rightConcept;
				// check if graph contains any of above
				Set<String> osConcepts = outputSpace.getVertexSet();
				String from;
				String to0, to1;
				if (osConcepts.contains(leftConcept)) {
					from = leftConcept;
					to0 = rightConcept;
					to1 = blendedConcept;
					// mutate or not?
					if (random.nextBoolean()) {
						swapVertex(random.nextBoolean(), outputSpace, from, to0, to1);
					}
				}
				if (osConcepts.contains(rightConcept)) {
					from = rightConcept;
					to0 = leftConcept;
					to1 = blendedConcept;
					// mutate or not?
					if (random.nextBoolean()) {
						swapVertex(random.nextBoolean(), outputSpace, from, to0, to1);
					}
				}
				if (osConcepts.contains(blendedConcept)) {
					from = blendedConcept;
					to0 = leftConcept;
					to1 = rightConcept;
					// mutate or not?
					if (random.nextBoolean()) {
						swapVertex(random.nextBoolean(), outputSpace, from, to0, to1);
					}
				}
			}
		}
	}

	public void mutateGenes(Blend[] genes, RandomGenerator random) {
		Blend blend = genes[0];

		// mutate mappings
		mutateMappings(random, blend);

		// mutate blend space /edges
		mutateEdges(random, blend);
	}

	private void mutateMappings(RandomGenerator random, Blend blend) {
		List<AnalogySet> analogies = Blend.getAnalogies();
		// once in a while, reset mappings to a full analogy
		if (random.nextDouble() < analogyResetProbability) {
			// initialize mappings from a random analogy
			int numberOfAnalogies = analogies.size();
			int analogyIndex = random.nextInt(numberOfAnalogies);
			AnalogySet analogy = analogies.get(analogyIndex);
			Set<Mapping<String>> original_mappings = analogy.getMappings();
			HashSet<Mapping<String>> mappings = new HashSet<Mapping<String>>(original_mappings);
			blend.setMappings(mappings);
		} else {
			// decide how many mappings to remove
			{
				Set<Mapping<String>> mappings = blend.getMappings();
				int n = mappings.size();
				if (n > 0) {
					double maxMappingsToRemove = (double) n * random.nextDouble() * 0.25;
					for (int i = 0; i < maxMappingsToRemove; i++) {
						// pick a random mapping to remove
						int indexToRemove = random.nextInt(mappings.size());
						Mapping<String> elementToRemove = GraphAlgorithms.getElementFromCollection(mappings, indexToRemove);
						mappings.remove(elementToRemove);
					}
				}
			}
			// decide how many mappings to add
			{
				Set<Mapping<String>> mappings = blend.getMappings();
				Set<Mapping<String>> mappingsFromAllAnalogies = Blend.getMappingsFromAllAnalogies();
				// maximum amount is all the existing in some analogy
				int numberOfAnalogies = analogies.size();
				int analogyIndex = random.nextInt(numberOfAnalogies);
				AnalogySet analogy = analogies.get(analogyIndex);
				int n = analogy.size();
				if (n > 0) {
					double maxMappingsToAdd = (double) n * random.nextDouble() * 0.25;
					for (int i = 0; i < maxMappingsToAdd; i++) {
						// pick a mapping from the set of all mappings (from all analogies)
						int indexToAdd = random.nextInt(mappingsFromAllAnalogies.size());
						Mapping<String> elementToAdd = GraphAlgorithms.getElementFromCollection(mappingsFromAllAnalogies, indexToAdd);
						// add it to the blend's mapping list
						mappings.add(elementToAdd);
					}
				}
			}
		}
	}

	public void swapVertex(boolean replaceTo0, StringGraph outputSpace, String from, String to0, String to1) {
		if (replaceTo0) {
			outputSpace.renameVertex(from, to0);
		} else {
			outputSpace.renameVertex(from, to1);
		}
	}
}
