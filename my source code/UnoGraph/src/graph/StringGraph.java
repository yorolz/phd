package graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A directed multigraph where both vertices and edges are String. A directed multigraph is a non-simple directed graph in which no loops are permitted, but multiple edges between
 * any two vertices are.
 *
 * @author Joao Goncalves: jcfgonc@gmail.com
 *
 *         version 1.3 added containsVertex()
 */
public class StringGraph implements Serializable {

	private static final long serialVersionUID = -1197786236652544325L;

	public static EdgeDirection getEdgeDirectionRelativeTo(String vertex, StringEdge edge) {
		if (isIncoming(vertex, edge))
			return EdgeDirection.INCOMING;
		if (isOutgoing(vertex, edge))
			return EdgeDirection.OUTGOING;
		return EdgeDirection.UNDEFINED;
	}

	public static boolean isIncoming(String vertex, StringEdge edge) {
		if (edge.getTarget().equals(vertex))
			return true;
		return false;
	}

	public static boolean isOutgoing(String vertex, StringEdge edge) {
		if (edge.getSource().equals(vertex))
			return true;
		return false;
	}

	public static boolean isTouching(String vertex, StringEdge edge) {
		if (edge.getTarget().equals(vertex) || edge.getSource().equals(vertex))
			return true;
		else
			return false;
	}

	// DirectedPseudograph<String, StringEdge> graph;
	private DirectedMultiGraphOld<String, StringEdge> graph;
	private final boolean allowSelfLoops = false;
	private final boolean allowSymmetry = false;

	public StringGraph(int numEdges, int inEdges, int outEdges, int numVertices) {
		this.graph = new DirectedMultiGraphOld<String, StringEdge>(numEdges, inEdges, outEdges, numVertices);
	}

	public StringGraph() {
		this.graph = new DirectedMultiGraphOld<String, StringEdge>();
	}

	public StringGraph(StringGraph otherGraph) {
		this.graph = new DirectedMultiGraphOld<String, StringEdge>(otherGraph.graph);
		addEdges(otherGraph);
	}

	public StringGraph(StringGraph otherGraph, boolean allocateOnly) {
		this.graph = new DirectedMultiGraphOld<String, StringEdge>(otherGraph.graph);
		if (!allocateOnly) {
			addEdges(otherGraph);
		}
	}

	/**
	 * Converts from generic DirectedMultiGraph to a StringGraph, using toString() on both vertices and edges.
	 * 
	 * @param otherGraph
	 */
	public <V, E> StringGraph(DirectedMultiGraphOld<V, E> otherGraph) {
		this.graph = new DirectedMultiGraphOld<String, StringEdge>();
		for (E edge : otherGraph.edgeSet()) {
			String edgeSource = otherGraph.getEdgeSource(edge).toString();
			String edgeTarget = otherGraph.getEdgeTarget(edge).toString();
			String edgeAsString;

			if (edge instanceof StringEdge) {
				StringEdge e = (StringEdge) edge;
				edgeAsString = e.getLabel();
			} else if (edge instanceof GraphEdge<?, ?>) {
				GraphEdge<?, ?> e = (GraphEdge<?, ?>) edge;
				edgeAsString = e.getLabel().toString();
			} else {
				edgeAsString = edge.toString();
			}

			addEdge(edgeSource, edgeTarget, edgeAsString);
		}
	}

	/**
	 * only copies the edges contained in the given mask
	 * 
	 * @param old
	 * @param mask
	 */
	public StringGraph(StringGraph old, Set<String> mask) {
		this(old, true);
		// TODO: check for divided components
		for (StringEdge edge : old.edgeSet()) {
			if (mask.contains(edge.getSource()) && mask.contains(edge.getTarget())) {
				this.addEdge(edge);
			}
		}
	}

	public void addEdges(Collection<StringEdge> edges) {
		for (StringEdge edge : edges) {
			addEdge(edge);
		}
	}

	public void addEdges(StringGraph otherGraph) {
		addEdges(otherGraph.edgeSet());
	}

	public void addEdges(Collection<StringEdge> edges, Set<String> mask) {
		for (StringEdge edge : edges) {
			if (mask.contains(edge.getSource()) && mask.contains(edge.getTarget())) {
				addEdge(edge);
			}
		}
	}

	public void addEdges(StringGraph otherGraph, Set<String> mask) {
		addEdges(otherGraph.edgeSet(), mask);
	}

	/**
	 * Adds two opposing directional edges connecting the given vertices
	 *
	 * @param vertex1Id
	 * @param vertex2Id
	 * @param edgeLabel
	 */
	public void addBidirectionalEdge(String vertex1Id, String vertex2Id, String edgeLabel) {
		addEdge(vertex1Id, vertex2Id, edgeLabel);
		addEdge(vertex2Id, vertex1Id, edgeLabel);
	}

	/**
	 * adds a the given labelled edge between two vertices
	 *
	 * @param source
	 * @param target
	 * @param edgeLabel
	 */
	public void addEdge(String source, String target, String label) {
		StringEdge edge = new StringEdge(source, target, label);

		if (!allowSelfLoops && source.equals(target)) {
			System.err.printf("LOOP: %s,%s,%s\n", source, label, target);
			return;
		}

		if (!allowSymmetry && this.edgeSet().contains(edge.reverse())) {
			System.err.printf("SYMMETRY: %s,%s,%s\n", source, label, target);
			return;
		}

		if (source.isEmpty() || target.isEmpty()) {
			System.err.printf("INVALID SOURCE||TARGET: %s,%s,%s\n", source, label, target);
			return;
		}

		if (label.isEmpty()) {
			System.err.printf("EMPTY RELATION: %s,%s,%s\n", source, label, target);
			return;
		}

		this.graph.addEdge(source, target, edge);
	}

	public void addEdge(StringEdge edge) {
		String source = edge.getSource();
		String target = edge.getTarget();
		String label = edge.getLabel();
		addEdge(source, target, label);
	}

	// public void addVertex(String label) {
	// this.graph.addVertex(label);
	// }

	/**
	 * clears this graph, removing all node and edge information
	 */
	public void clear() {
		graph.clear();
	}

	public boolean containsVertex(String vertex) {
		return this.graph.containsVertex(vertex);
	}

	public Set<StringEdge> edgeSet() {
		return graph.edgeSet();
	}

	public Set<StringEdge> edgeSet(String edgeLabel) {
		HashSet<StringEdge> edges = new HashSet<>(1 << 10);
		for (StringEdge edge : edgeSet()) {
			if (edge.getLabel().equals(edgeLabel)) {
				edges.add(edge);
			}
		}
		return edges;
	}

	/**
	 * Returns both incoming and outgoing edges from the given vertex
	 *
	 * @param vertex
	 * @return
	 */
	public Set<StringEdge> edgesOf(String vertex) {
		return graph.edgesOf(vertex);
	}

	public HashSet<StringEdge> edgesOf(String vertex, String filter) {
		HashSet<StringEdge> filtered = new HashSet<>(1 << 10);
		Set<StringEdge> edgesOf = edgesOf(vertex);
		for (StringEdge edge : edgesOf) {
			if (edge.getLabel().equals(filter)) {
				filtered.add(edge);
			}
		}
		return filtered;
	}

	/**
	 * Returns the set of all edges in this graph connecting both vertices (in both directions).
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	public Set<StringEdge> getBidirectedEdges(String vertex0, String vertex1) {
		Set<StringEdge> edgeSet0 = getDirectedEdges(vertex0, vertex1);
		Set<StringEdge> edgeSet1 = getDirectedEdges(vertex1, vertex0);
		HashSet<StringEdge> edgeSet = new HashSet<>((edgeSet0.size() + edgeSet1.size()) * 2 + 16);
		edgeSet.addAll(edgeSet0);
		edgeSet.addAll(edgeSet1);
		return edgeSet;
	}

	public int getDegree(String concept) {
		return degreeOf(concept);
	}

	/**
	 * Returns the degree (number of connected edges) of the given vertex
	 *
	 * @param concept
	 * @return
	 */
	public int degreeOf(String concept) {
		// return graph.degreeOf(vertexId);
		return graph.degreeOf(concept);
		// return graph.edgesOf(vertexId).size();
	}

	/**
	 * Returns either outgoing or incoming edges to the given vertex
	 *
	 * @param outgoing true if to return outgoing, incoming otherwise
	 * @param concept
	 * @return
	 */
	public Set<StringEdge> getDirectedEdges(boolean outgoing, String concept) {
		if (outgoing)
			return this.outgoingEdgesOf(concept);
		else
			return this.incomingEdgesOf(concept);
	}

	/**
	 * get edges going from source to target
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	public Set<StringEdge> getDirectedEdges(String source, String target) {
		Set<StringEdge> edgeSet = graph.getEdges(source, target);
		return edgeSet;
	}

	/**
	 * Returns the set of all edges in this graph connecting the given source to the given target with the given relation.
	 *
	 * @param source
	 * @param target
	 * @param relation
	 * @return
	 */
	public Set<StringEdge> getDirectedEdgesWithRelationEqualTo(String source, String target, String relation) {
		HashSet<StringEdge> set = new HashSet<>(1 << 10);
		Set<StringEdge> edges = getDirectedEdges(source, target);
		if (edges != null) {
			for (StringEdge edge : edges) {
				if (edge.getLabel().equals(relation))
					set.add(edge);
			}
		}
		return set;
	}

	public int getInDegree(String vertexId) {
		return graph.inDegreeOf(vertexId);
	}

	/**
	 * Returns the set of vertices connected to the given vertex
	 *
	 * @param vertex
	 * @return
	 */
	public Set<String> getNeighborVertices(String vertex) {
		Set<StringEdge> edgesI = graph.incomingEdgesOf(vertex);
		Set<StringEdge> edgesO = graph.outgoingEdgesOf(vertex);

		HashSet<String> neighbors = new HashSet<>((edgesI.size() + edgesO.size()) * 2 + 16);

		for (StringEdge edge : edgesI) {
			String otherConcept = edge.getOppositeOf(vertex);
			neighbors.add(otherConcept);
		}

		for (StringEdge edge : edgesO) {
			String otherConcept = edge.getOppositeOf(vertex);
			neighbors.add(otherConcept);
		}

		return neighbors;
	}

	public Set<String> getIncomingVertices(String vertex) {
		Set<StringEdge> edges = incomingEdgesOf(vertex);
		HashSet<String> sources = edgesSources(edges);
		sources.remove(vertex);
		return sources;
	}

	private HashSet<String> edgesSources(Set<StringEdge> edges) {
		HashSet<String> sources = new HashSet<>(1 << 10);
		for (StringEdge edge : edges) {
			String source = graph.getEdgeSource(edge);
			sources.add(source);
		}
		// this may contain the specified vertex, so remove it
		return sources;
	}

	public Set<String> getOutgoingVertices(String vertex) {
		Set<StringEdge> edges = outgoingEdgesOf(vertex);
		HashSet<String> targets = edgesTargets(edges);
		targets.remove(vertex);
		return targets;
	}

	private HashSet<String> edgesTargets(Set<StringEdge> edges) {
		HashSet<String> targets = new HashSet<>(1 << 10);
		for (StringEdge edge : edges) {
			String target = graph.getEdgeTarget(edge);
			targets.add(target);
		}
		// this may contain the specified vertex, so remove it
		return targets;
	}

	public int getOutDegree(String vertexId) {
		return graph.outDegreeOf(vertexId);
	}

	/**
	 * Returns the set of vertices contained in this graph and which participate in edges.
	 *
	 * @return
	 */
	public Set<String> getVertexSet() {
		Set<String> vertexSet = this.graph.vertexSet();
		return vertexSet;
	}

	public Set<StringEdge> incomingEdgesOf(String vertex) {
		return graph.incomingEdgesOf(vertex);
	}

	public Set<StringEdge> incomingEdgesOf(String concept, String filter) {
		Set<StringEdge> incoming = incomingEdgesOf(concept);
		HashSet<StringEdge> edges = new HashSet<>(incoming.size() + 16);
		for (StringEdge edge : incoming) {
			if (edge.getLabel().equals(filter)) {
				edges.add(edge);
			}
		}
		return edges;
	}

	public int numberOfEdges() {
		return graph.edgeSet().size();
	}

	public int numberOfEdges(String label) {
		int num = 0;
		for (StringEdge edge : edgeSet()) {
			if (edge.getLabel().equals(label)) {
				num++;
			}
		}
		return num;
	}

	public int numberOfVertices() {
		return graph.vertexSet().size();
	}

	public Set<StringEdge> outgoingEdgesOf(String vertex) {
		return graph.outgoingEdgesOf(vertex);
	}

	public Set<StringEdge> outgoingEdgesOf(String concept, String filter) {
		Set<StringEdge> out = outgoingEdgesOf(concept);
		HashSet<StringEdge> edges = new HashSet<>(out.size() + 16);
		for (StringEdge edge : out) {
			if (edge.getLabel().equals(filter)) {
				edges.add(edge);
			}
		}
		return edges;
	}

	public void removeEdge(StringEdge edgeToDelete) {
		graph.removeEdge(edgeToDelete);
		// String source = edgeToDelete.getSource();
		// String target = edgeToDelete.getTarget();
		// if (containsVertex(source) && edgesOf(source).isEmpty()) {
		// removeVertex(source);
		// }
		// if (containsVertex(target) && edgesOf(target).isEmpty()) {
		// removeVertex(target);
		// }
	}

	public void removeLoops() {
		Set<StringEdge> edges = this.edgeSet();
		ArrayList<StringEdge> toRemove = new ArrayList<>();
		for (StringEdge edge : edges) {
			if (edge.isLoop()) {
				toRemove.add(edge);
			}
		}

		for (StringEdge edge : toRemove) {
			this.removeEdge(edge);
		}
	}

	/**
	 * Removes the specified vertex from this graph including all its touching edges if present.
	 *
	 * @param vertex
	 * @return true if the graph contained the specified vertex; false otherwise.
	 */
	public void removeVertex(String vertex) {
		graph.removeVertex(vertex);
	}

	public void removeVertices(Collection<String> vertices) {
		for (String v : vertices) {
			removeVertex(v);
		}
	}

	/**
	 * VALIDATED.
	 * 
	 * @param edge
	 * @param original
	 * @param replacement
	 */
	public void replaceEdgeSourceOrTarget(StringEdge edge, String original, String replacement) {
		StringEdge newEdge = edge.replaceSourceOrTarget(original, replacement);
		removeEdge(edge);
		addEdge(newEdge);
	}

	/**
	 * @param original
	 * @param replacement
	 */
	public void replaceVertex(String original, String replacement) {
		renameVertex(original, replacement);
	}

	public void renameVertex(String original, String replacement) {
		if (original.equals(replacement))
			return;
		// remove old edges touching the old vertex while adding new edges with the replaced vertex
		ArrayList<StringEdge> toAdd = new ArrayList<>();
		ArrayList<StringEdge> toRemove = new ArrayList<>();
		for (StringEdge edge : graph.edgesOf(original)) {
			StringEdge newEdge = edge.replaceSourceOrTarget(original, replacement);
			toAdd.add(newEdge);
			toRemove.add(edge);
		}
		removeEdges(toRemove);
		addEdges(toAdd);
	}

	public String toString(final int limit, final int lineBreak) {
		int counter = 0;
		String buffer = "";
		for (StringEdge edge : edgeSet()) {
			buffer += edge.toString() + ";";
			if (counter % lineBreak == 0 && counter > 0)
				buffer += System.lineSeparator();
			if (counter > limit)
				break;
			counter++;
		}
		// return graph.toString();
		return buffer;
	}

	public String toString() {
		return toString(64, Integer.MAX_VALUE);
	}

	public void removeEdges(Collection<StringEdge> toRemove) {
		graph.removeEdges(toRemove);
	}

	public boolean containsEdge(StringEdge se) {
		return this.graph.containsEdge(se);
	}

	public void showStructureSizes() {
		graph.showStructureSizes();
	}

	public Collection<String> getEdgeLabelSet() {
		HashSet<String> edgeLabels = new HashSet<>();
		for (StringEdge edge : edgeSet()) {
			edgeLabels.add(edge.getLabel());
		}
		return edgeLabels;
	}

	public boolean isEmpty() {
		return edgeSet().isEmpty();
	}

	public void removeEdges(String relationFilter) {
		Set<StringEdge> edges = edgeSet(relationFilter);
		removeEdges(edges);
	}

	public HashSet<StringEdge> getNeighbourEdges(StringEdge edge) {
		return getTouchingEdges(edge);
	}

	public HashSet<StringEdge> getTouchingEdges(StringEdge edge) {
		String source = edge.getSource();
		String target = edge.getTarget();

		Set<StringEdge> sourceEdges = this.edgesOf(source);
		Set<StringEdge> targetEdges = this.edgesOf(target);

		HashSet<StringEdge> touching = new HashSet<>((sourceEdges.size() + targetEdges.size()) * 2 + 16);
		touching.addAll(sourceEdges);
		touching.addAll(targetEdges);
		touching.remove(edge);

		return touching;
	}

	public HashSet<StringEdge> edgesOf(Collection<String> vertices) {
		HashSet<StringEdge> edges = new HashSet<StringEdge>();
		for (String vertex : vertices) {
			edges.addAll(edgesOf(vertex));
		}
		return edges;
	}

	public HashSet<StringEdge> edgesOf(Set<String> vertices, String filter) {
		HashSet<StringEdge> edges = new HashSet<StringEdge>();
		for (String vertex : vertices) {
			edges.addAll(edgesOf(vertex, filter));
		}
		return edges;
	}
}
