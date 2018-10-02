package sapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.StringEdge;
import graph.StringGraph;

public class SapperMappingRules {

	/**
	 * applies the squaring rule to all the known dormant bridges, building a set of new dormant bridges
	 * 
	 * @param graph
	 * @param newDormantBridges
	 *            the set where to put the new dormant bridges
	 * @param oldDormantBridges
	 *            the list of existing bridges to be explored
	 * @throws InterruptedException
	 */
	public static void applySquaringRule(StringGraph graph, Set<DormantBridge> newDormantBridges, List<DormantBridge> oldDormantBridges) throws InterruptedException {
		for (DormantBridge db : oldDormantBridges) {
			newDormantBridges = SapperMappingRules.squaringRule(graph, db);
		}
	}

	/**
	 * applies the triangulation rule to the graph, adding to an existing set the new discovered dormant bridges
	 * 
	 * @param graph
	 * @param dormantBridges
	 *            the set where to put the discovered dormant bridges
	 * @return
	 * @throws InterruptedException
	 */
	public static HashSet<DormantBridge> applyTriangulationRule(StringGraph graph) throws InterruptedException {
		HashSet<DormantBridge> dormantBridges = new HashSet<>();
		for (String startingVertex : graph.getVertexSet()) {
			dormantBridges.addAll(SapperMappingRules.triangulationRule(startingVertex, graph));
		}
		return dormantBridges;
	}

	public static HashSet<DormantBridge> squaringRule(StringGraph graph, DormantBridge db) {
		HashSet<DormantBridge> newDormantBridges = new HashSet<>();
		// para um dado mapeamento A<=>B, percorrer todas as arestas que chegam
		// a A e a B (e quadratico, mas talvez de para optimizar)
		// se cada par de arestas ->A e ->B tiver o mesmo label, cria uma
		// dormant bridge com os vertices ligados a origem de ambas as arestas
		String b = db.getConcept0();
		String c = db.getConcept1();
		Set<StringEdge> edgesToB = graph.incomingEdgesOf(b);
		Set<StringEdge> edgesToC = graph.incomingEdgesOf(c);
		for (StringEdge edgeToB : edgesToB) {
			for (StringEdge edgeToC : edgesToC) {
				String a = edgeToB.getSource();
				String d = edgeToC.getSource();
				String edgeToBLabel = edgeToB.getLabel();
				String edgeToCLabel = edgeToC.getLabel();
				if (similarRelation(edgeToBLabel, edgeToCLabel)) {
					DormantBridge newDB = new DormantBridge(a, d);
					newDormantBridges.add(newDB); // another structure to prevent concurrent modification exception
				}
			}
		}
		return newDormantBridges;
	}

	public static HashSet<DormantBridge> triangulationRule(String startingVertex, StringGraph graph) {
		HashSet<DormantBridge> dormantBridges = new HashSet<>();
		String vertexA = startingVertex;
		Set<StringEdge> edgesAB = graph.outgoingEdgesOf(vertexA);
		for (StringEdge edgeAB : edgesAB) {
			String vertexB = edgeAB.getTarget();
			// prevent loop on self
			if (vertexB.equals(vertexA))
				continue;
			// expand on b
			Set<StringEdge> edgesBC = graph.incomingEdgesOf(vertexB);
			for (StringEdge edgeBC : edgesBC) {
				String vertexC = edgeBC.getSource();
				// prevent loop on self OR loop to first vertex
				if (vertexC.equals(vertexB) || vertexC.equals(vertexA))
					continue;
				// check between a & c
				boolean edgesMatch = similarRelation(edgeAB.getLabel(), edgeBC.getLabel());
				if (edgesMatch) {
					// TODO: check if the db's vertices have already a relation
					// getting neighbors is independent of direction, so just ask for one direction
					if (graph.getNeighborVertices(vertexA).contains(vertexC))
						continue;
					DormantBridge dormantBridge = new DormantBridge(vertexA, vertexC);
					dormantBridges.add(dormantBridge);
				}
			}
		}
		return dormantBridges;
	}

	/**
	 * to be implemented as an interface
	 * 
	 * @param edgeLabel0
	 * @param edgeLabel1
	 * @return true if the labels match according to some criteria
	 */
	private static boolean similarRelation(String edgeLabel0, String edgeLabel1) {
		return edgeLabel0.equals(edgeLabel1);
	}
}
