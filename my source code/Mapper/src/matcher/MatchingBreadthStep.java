package matcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import graph.EdgeDirection;
import graph.StringEdge;
import graph.StringGraph;
import structures.MapOfSet;

public class MatchingBreadthStep {
	public static boolean debug = Config.DEBUG;

	/**
	 * returns the intersection of the given sets
	 *
	 * @param vertexSet0
	 * @param vertexSet1
	 * @return
	 */
	public static Set<String> intersection(Set<String> vertexSet0, Set<String> vertexSet1) {
		HashSet<String> intersection = new HashSet<String>();

		if (vertexSet0 == null || vertexSet1 == null)
			return intersection;

		for (String vertex0 : vertexSet0) {
			if (vertexSet1.contains(vertex0)) {
				intersection.add(vertex0);
			}
		}
		return intersection;
	}

	private HashMap<String, String> ancestor;
	private HashSet<String> closedSet;
	private int currentDepth;
	private MapOfSet<Integer, String> depthToVertexSet;
	private StringGraph graph;
	private ArrayDeque<String> openSet;
	// private MapSet<EdgeDirection, String> directionToVertexSet; // direction of the edge touching the vertex (vertex relative)
	private MapOfSet<String, String> relationToVertexSet; // relation of the edge touching the vertex (vertex relative)
	private HashSet<String> rightVisitedConcepts;

	public MatchingBreadthStep(StringGraph graph, String referenceVertex) {
		this.graph = graph;
		this.depthToVertexSet = new MapOfSet<>();
		this.closedSet = new HashSet<>();
		this.relationToVertexSet = new MapOfSet<>();
		this.ancestor = new HashMap<>();
		this.openSet = new ArrayDeque<>();
		this.rightVisitedConcepts = new HashSet<>();
		// this.directionToVertexSet = new MapSet<>();
		currentDepth = 0;

		// vertex to start expanding
		// deepness is the distance from each expanded set of "breadth" vertices to the reference vertex
		openSet.addLast(referenceVertex);
		depthToVertexSet.put(currentDepth, referenceVertex);
		if (debug)
			System.out.println("right opset:" + openSet + " deep:" + currentDepth);
	}

	public HashMap<String, String> getAncestor() {
		return ancestor;
	}

	public HashSet<String> getClosedSet() {
		return closedSet;
	}

	public int getCurrentDepth() {
		return currentDepth;
	}

	public MapOfSet<Integer, String> getDepthToVertexSet() {
		return depthToVertexSet;
	}

	public StringGraph getGraph() {
		return graph;
	}

	public ArrayDeque<String> getOpenSet() {
		return openSet;
	}

	public MapOfSet<String, String> getRelationToVertexSet() {
		return relationToVertexSet;
	}

	public HashSet<String> getRightVisitedConcepts() {
		return rightVisitedConcepts;
	}

	/**
	 * returns the set of vertices at the specified deepness which were expanded by going through an edge with the given relation label
	 *
	 * @param deepness
	 * @param relationLabel
	 * @param edgeDirection
	 * @return
	 */
	public Set<String> getVertices(int deepness, String edgeLabel, Set<String> precedence) {
		Set<String> setVerticesByRelation = this.relationToVertexSet.get(edgeLabel);
		Set<String> setVerticesByDeepness = this.depthToVertexSet.get(deepness);

		Set<String> vertexSet = intersection(setVerticesByRelation, setVerticesByDeepness);
		Set<String> set = new HashSet<>();

		// for each of the above vertices, exclude the ones which are not accessible from the set of vertices in the precedence set
		for (String vertex : vertexSet) {
			for (String ancestorVertex : precedence) {
				if (ancestor.get(vertex).equals(ancestorVertex)) {
					set.add(vertex);
					break;
				}
			}
		}
		return set;
	}

	public Set<String> getVertices(int currentDeepness, String relation, Set<String> rightMatchAncestor, EdgeDirection givenEdgeDirection) {
		HashSet<String> set = new HashSet<>();
		Set<String> targets = getVertices(currentDeepness, relation, rightMatchAncestor);
		for (String target : targets) {
			String source = this.ancestor.get(target);
			Set<StringEdge> edges = graph.getBidirectedEdges(source, target);
			for (StringEdge edge : edges) {
				EdgeDirection currentEdgeDir = StringGraph.getEdgeDirectionRelativeTo(target, edge);
				if (currentEdgeDir.equals(givenEdgeDirection)) {
					set.add(target);
				}
			}
		}
		return set;
	}

	public void updateDepthSets(int deepnessToAchieve, HashSet<String> leftVisitedConcepts) {
		while (currentDepth < deepnessToAchieve) {
			// do an iteration (expansion)
			ArrayList<String> nextDepthSet = new ArrayList<String>();
			while (!openSet.isEmpty()) {
				String currentVertex = openSet.removeLast();
				rightVisitedConcepts.add(currentVertex);

				if (closedSet.contains(currentVertex))
					continue;

				// ----------------
				Set<StringEdge> expandedEdges = graph.edgesOf(currentVertex);// graph.incomingEdgesOf(currentVertex);
				for (StringEdge expandedEdge : expandedEdges) {

					String expandedNeighbor = expandedEdge.getOppositeOf(currentVertex); // expandedEdge.getSource();

					// we may be going backwards
					if (closedSet.contains(expandedNeighbor))
						continue;
					// was expanded before but not yet visited
					if (openSet.contains(expandedNeighbor))
						continue;
					// we may be colliding with the left set
					if (leftVisitedConcepts.contains(expandedNeighbor))
						continue;

					// store expanded vertex and the vertex which expanded to it
					ancestor.put(expandedNeighbor, currentVertex);
					rightVisitedConcepts.add(expandedNeighbor);

					// add edge label from current to neighbor vertex
					String relation = expandedEdge.getLabel();
					relationToVertexSet.put(relation, expandedNeighbor);
					// EdgeDirection edgeDirection = graph.getEdgeDirectionRelativeTo(expandedNeighbor, expandedEdge);
					// directionToVertexSet.putSetElement(edgeDirection, expandedNeighbor);

					nextDepthSet.add(expandedNeighbor);
				}
				closedSet.add(currentVertex);
			}
			// add neighbor vertices not expanded before
			currentDepth++;
			if (!nextDepthSet.isEmpty()) {
				openSet.addAll(nextDepthSet);
				// reached new deepness level, update depth to vertex set mapping
				depthToVertexSet.put(currentDepth, nextDepthSet);
			}
			if (debug)
				System.out.println("right opset:" + nextDepthSet + " depth:" + currentDepth);
		}
	}
}
