package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import structures.MapOfSet;

/**
 * High Performance Directed MultiGraph. This graph is problematic because of internal edge conflicts.
 * 
 * @author jcfgonc@gmail.com
 *
 * @param <V>
 *            Vertex Class
 * @param <E>
 *            Edge Class
 * @deprecated
 */
public class DirectedMultiGraphOld<V, E> {
	private HashSet<E> edgeSet;
	private HashMap<E, V> edgeSource;
	private HashMap<E, V> edgeTarget;
	private MapOfSet<V, E> incomingEdges;
	private MapOfSet<V, E> outgoingEdges;
	private HashSet<V> vertexSet;

	public DirectedMultiGraphOld(int numEdges, int inEdges, int outEdges, int numVertices) {
		edgeSet = new HashSet<>(numEdges);
		edgeSource = new HashMap<>(numEdges);
		edgeTarget = new HashMap<>(numEdges);
		incomingEdges = new MapOfSet<>(inEdges);
		outgoingEdges = new MapOfSet<>(outEdges);
		vertexSet = new HashSet<>(numVertices);
	}

	public DirectedMultiGraphOld() {
		edgeSet = new HashSet<>();
		edgeSource = new HashMap<>();
		edgeTarget = new HashMap<>();
		incomingEdges = new MapOfSet<>();
		outgoingEdges = new MapOfSet<>();
		vertexSet = new HashSet<>();
	}

	/**
	 * This only allocates structures' sizes, it does not copy data!
	 * 
	 * @param graph
	 */
	public DirectedMultiGraphOld(DirectedMultiGraphOld<V, E> graph) {
		this(graph.edgeSet.size(), graph.incomingEdges.size(), graph.outgoingEdges.size(), graph.vertexSet.size());
	}

	public void showStructureSizes() {
		System.out.println("edgeSet: " + edgeSet.size());
		System.out.println("incomingEdges: " + incomingEdges.size());
		System.out.println("outgoingEdges: " + outgoingEdges.size());
		System.out.println("vertexSet: " + vertexSet.size());
	}

	public void addEdge(V source, V target, E edge) {

		incomingEdges.put(target, edge);
		outgoingEdges.put(source, edge);
		edgeSource.put(edge, source);
		edgeTarget.put(edge, target);
		edgeSet.add(edge);

		vertexSet.add(source);
		vertexSet.add(target);
	}

	public boolean containsEdge(E se) {
		return edgeSet.contains(se);
	}

	public boolean containsVertex(V vertex) {
		return vertexSet.contains(vertex);
	}

	public int degreeOf(V vertexId) {
		int d = inDegreeOf(vertexId) + outDegreeOf(vertexId);
		return d;
	}

	/**
	 * unsafe, returns internal set of edges
	 * 
	 * @return
	 */
	public Set<E> edgeSet() {
		return edgeSet;
	}

	public Set<E> edgesOf(V vertex) {
		// union of
		Set<E> in = incomingEdgesOf(vertex);
		// and
		Set<E> out = outgoingEdgesOf(vertex);

		int initialCapacity = in.size() + out.size();
		HashSet<E> set = new HashSet<>(initialCapacity * 2);
		set.addAll(in);
		set.addAll(out);

		return set;
	}

	/**
	 * get edges outgoing from v0 incoming to v1
	 * 
	 * @param v0
	 * @param v1
	 * @return
	 */
	public Set<E> getEdges(V v0, V v1) {
		// intersection of
		Set<E> in = incomingEdgesOf(v1);
		Set<E> out = outgoingEdgesOf(v0);
		// and

		boolean emptyIn = in == null || in.isEmpty();
		boolean emptyOut = out == null || out.isEmpty();

		if (emptyIn || emptyOut) {
			return new HashSet<>(1);
		}

		Set<E> intersection = new HashSet<>(in);
		intersection.retainAll(out);

		return intersection;
	}

	public V getEdgeSource(E edge) {
		return edgeSource.get(edge);
	}

	public V getEdgeTarget(E edge) {
		return edgeTarget.get(edge);
	}

	public Set<E> incomingEdgesOf(V vertex) {
		Set<E> set = incomingEdges.get(vertex);
		if (set == null) {
			return new HashSet<>();
		}
		return set;
	}

	public int inDegreeOf(V vertexId) {
		Set<E> i = incomingEdgesOf(vertexId);
		if (i == null) {
			return 0;
		}
		return i.size();
	}

	public int outDegreeOf(V vertexId) {
		Set<E> o = outgoingEdgesOf(vertexId);
		if (o == null) {
			return 0;
		}
		return o.size();
	}

	public Set<E> outgoingEdgesOf(V vertex) {
		Set<E> set = outgoingEdges.get(vertex);
		if (set == null) {
			return new HashSet<>();
		}
		return set;
	}

	public void removeEdge(E edge) {
		if(!containsEdge(edge))
			return;
		
		V target = getEdgeTarget(edge);
		Set<E> si = incomingEdges.get(target);
		if (si != null) {
			si.remove(edge);
			if(si.isEmpty())
				incomingEdges.removeKey(target);
		}
		
		V source = getEdgeSource(edge);
		Set<E> so = outgoingEdges.get(source);
		if (so != null) {
			so.remove(edge);
			if(so.isEmpty())
				outgoingEdges.removeKey(source);
		}

		if (degreeOf(source) == 0) {
			vertexSet.remove(source);
		}
		if (degreeOf(target) == 0) {
			vertexSet.remove(target);
		}
		
		edgeSet.remove(edge);
		edgeSource.remove(edge);
		edgeTarget.remove(edge);
	}

	public void removeEdges(Collection<E> toRemove) {
		for (E edge : toRemove) {
			removeEdge(edge);
		}
	}

	public void removeVertex(V vertex) {
		if (!containsVertex(vertex))
			return;

		Set<E> in = incomingEdgesOf(vertex);
		Set<E> out = outgoingEdgesOf(vertex);

		ArrayList<E> list = new ArrayList<>(in.size() + out.size());

		list.addAll(in);
		list.addAll(out);

		for (E edge : list) {
			removeEdge(edge);
		}
	}

	/**
	 * unsafe, returns internal set of vertices
	 * 
	 * @return
	 */
	public Set<V> vertexSet() {
		return vertexSet;
	}

	public void clear() {
		edgeSet = new HashSet<>();
		edgeSource = new HashMap<>();
		edgeTarget = new HashMap<>();
		incomingEdges = new MapOfSet<>();
		outgoingEdges = new MapOfSet<>();
		vertexSet = new HashSet<>();
	}

}
