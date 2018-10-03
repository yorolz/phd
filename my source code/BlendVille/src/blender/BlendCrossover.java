package blender;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import blender.structures.Mapping;
import genetic.Chromosome;
import genetic.operators.GeneCrossover;
import graph.StringEdge;
import graph.StringGraph;

public class BlendCrossover implements GeneCrossover<Blend> {
	private final int graphCrossoverMode = 1;

	public void crossover(Chromosome<Blend> parent0, Chromosome<Blend> parent1, Chromosome<Blend> offspring0, Chromosome<Blend> offspring1,
			RandomGenerator random) {

		Blend parentBlend0 = parent0.genes[0];
		Blend parentBlend1 = parent1.genes[0];

		// set up a clean blend space for each child
		offspring0.genes[0] = new Blend(parentBlend0, false, false);
		offspring1.genes[0] = new Blend(parentBlend1, false, false);

		offspring0.genes[0].outputSpace = new StringGraph();
		offspring1.genes[0].outputSpace = new StringGraph();

		// first crossover the mappings
		uniformCrossoverOfMappings(parent0.genes[0], parent1.genes[0], offspring0.genes[0], offspring1.genes[0], random);

		// now crossover the graphs
		switch (graphCrossoverMode) {
		case 0:
			uniformCrossover(parent0.genes[0], parent1.genes[0], offspring0.genes[0], offspring1.genes[0], random);
			break;
		case 1:
			radialCrossover(parent0.genes[0], parent1.genes[0], offspring0.genes[0], offspring1.genes[0], random);
			break;
		case 2:
			copyCrossover(parent0.genes[0], offspring0.genes[0]);
			copyCrossover(parent1.genes[0], offspring1.genes[0]);
			break;
		}
	}

	private void copyCrossover(Blend parent, Blend offspring) {
		StringGraph os0 = offspring.getOutputSpace();
		os0.addEdges(parent.getOutputSpace().edgeSet());
	}

	private void crossoverPivot(RandomGenerator random, Blend parentBlend0, Blend parentBlend1, String pivot, StringGraph os0,
			StringGraph os1) {
		HashSet<String> closedSet = new HashSet<>();
		ArrayDeque<String> openSet = new ArrayDeque<>();
		openSet.addLast(pivot);
		StringGraph sourceBlend = parentBlend0.outputSpace;
		boolean firstPhase = true;

		while (!openSet.isEmpty()) {
			String currentVertex = openSet.removeFirst();
			// higher probability will swap constantly the receiving offspring
			// this occurs when expanding a new vertex for its neighbors
			boolean swapCrossover = false;
			if (random.nextBoolean()) {
				swapCrossover = true;
			}
			// expand a single vertex
			// not in the closed set
			if (!closedSet.contains(currentVertex)) {
				// get the vertex neighbors not in the closed set
				Set<String> neighbors = sourceBlend.getNeighborVertices(currentVertex);
				for (String neighborId : neighbors) {
					if (closedSet.contains(neighborId))
						continue;
					// put the neighbors in the open set
					openSet.addLast(neighborId);
					//TODO not sure if this is bidirectional
					Set<StringEdge> edges = sourceBlend.getBidirectedEdges(currentVertex, neighborId);
					if (swapCrossover) {
						os0.addEdges(edges);
					} else {
						os1.addEdges(edges);
					}
				}
				// vertex from the open set explored, remove it from further exploration
				closedSet.add(currentVertex);
			}

			// swap source blend if done from parent0 to parent1
			if (openSet.isEmpty() && firstPhase) {
				sourceBlend = parentBlend1.outputSpace;
				firstPhase = false;
			}
		}
	}

	private void uniformCrossoverOfMappings(Blend parent0, Blend parent1, Blend offspring0, Blend offspring1, RandomGenerator random) {
		Set<Mapping<String>> p0mappings = parent0.getMappings();
		Set<Mapping<String>> p1mappings = parent1.getMappings();

		HashSet<Mapping<String>> off0mappings = new HashSet<Mapping<String>>();
		HashSet<Mapping<String>> off1mappings = new HashSet<Mapping<String>>();
		offspring0.setMappings(off0mappings);
		offspring1.setMappings(off1mappings);

		if (!p0mappings.isEmpty()) {
			for (Mapping<String> mapping : p0mappings) {
				if (random.nextBoolean()) {
					// copy from parent0 to child0
					off0mappings.add(mapping);
				} else {
					// copy from parent0 to child1
					off1mappings.add(mapping);
				}
			}
		}

		if (!p1mappings.isEmpty()) {
			for (Mapping<String> mapping : p1mappings) {
				if (random.nextBoolean()) {
					// copy from parent1 to child0
					off0mappings.add(mapping);
				} else {
					// copy from parent1 to child1
					off1mappings.add(mapping);
				}
			}
		}
	}

	private void radialCrossover(Blend parent0, Blend parent1, Blend offspring0, Blend offspring1, RandomGenerator random) {
		StringGraph os0 = offspring0.getOutputSpace();
		StringGraph os1 = offspring1.getOutputSpace();

		Set<String> intersection = Blend.intersectConcepts(parent0.outputSpace.getVertexSet(), parent1.outputSpace.getVertexSet());

		for (String pivot : intersection) {
			// executing this more than once probably increases average information
			crossoverPivot(random, parent0, parent1, pivot, os0, os1);
			if (os0.numberOfVertices() > 0 && os1.numberOfVertices() > 0) {
				break;
			}
		}

		if (os0.numberOfVertices() == 0) {
			if (random.nextBoolean())
				os0.addEdges(parent0.outputSpace.edgeSet());
			else
				os0.addEdges(parent1.outputSpace.edgeSet());
		}
		if (os1.numberOfVertices() == 0) {
			if (random.nextBoolean())
				os1.addEdges(parent0.outputSpace.edgeSet());
			else
				os1.addEdges(parent1.outputSpace.edgeSet());
		}
	}

	private void uniformCrossover(Blend parent0, Blend parent1, Blend offspring0, Blend offspring1, RandomGenerator random) {
		StringGraph os0 = offspring0.getOutputSpace();
		StringGraph os1 = offspring1.getOutputSpace();
		// get edge set for each parent
		// iterate each edge set
		for (StringEdge edge : parent0.outputSpace.edgeSet()) {
			// on average, copy half edges from one parent to one child
			if (random.nextBoolean())
				os0.addEdge(edge);
			else
				os1.addEdge(edge);
		}
		for (StringEdge edge : parent1.outputSpace.edgeSet()) {
			// on average, copy half edges from one parent to one child
			if (random.nextBoolean())
				os0.addEdge(edge);
			else
				os1.addEdge(edge);
		}
	}
}
