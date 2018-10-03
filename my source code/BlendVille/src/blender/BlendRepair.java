package blender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import blender.structures.Mapping;
import genetic.operators.GeneRepair;
import graph.StringGraph;

public class BlendRepair implements GeneRepair<Blend> {

	@Override
	public void repairGenes(Blend[] genes) {
		Blend blend = genes[0];

		// if it has no vertices or edges, refill it
		// while (blend.outputSpace.numberOfVertices() == 0 || blend.outputSpace.numberOfEdges() == 0) {
		// blend.initialize();
		// }

		// validate and check invalid combination of mappings
		{
			// shuffle list of mappings to allow stochastic execution
			ArrayList<Mapping<String>> mappingList = new ArrayList<>(blend.getMappings());
			HashSet<Mapping<String>> newMappingSet = new HashSet<>();
			Collections.shuffle(mappingList);
			// iterate through all the mappings, putting all concepts from the mappings in a "existing" set
			HashSet<String> existingConcepts = new HashSet<>();
			boolean modified = false;
			for (Mapping<String> mapping : mappingList) {
				boolean containsLeft = existingConcepts.contains(mapping.getLeftConcept());
				boolean containsRight = existingConcepts.contains(mapping.getRightConcept());
				// check if any concept in the current mapping has already been used
				if (containsLeft || containsRight) {
					// in that case, that mapping can not be used (as it will be incompatible with an existing mapping)
					modified = true;
					continue;
				} else {
					newMappingSet.add(mapping);
				}
			}
			if (modified) {
				blend.setMappings(newMappingSet);
			}
		}

		// remove loops (you never know)
		{
			StringGraph outputSpace = blend.getOutputSpace();
			outputSpace.removeLoops();
		}
	}
}
