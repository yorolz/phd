package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mapper.Mapping;
import mapper.OrderedPair;
import structures.ListOfSet;
import structures.MapOfList;
import structures.MapOfSet;
import structures.ObjectIndex;

public class GraphAlgorithms {
	public interface ExpandingEdge {
		public void expanding(String from, String to);
	}

	public static void addConceptsToSet(Collection<OrderedPair<String>> comboi, Set<String> existingConcepts) {
		for (OrderedPair<String> mapping : comboi) {
			existingConcepts.add(mapping.getLeftElement());
			existingConcepts.add(mapping.getRightElement());
		}
	}

	public static boolean combinationContainsConcepts(ArrayList<OrderedPair<String>> conceptCombination, HashSet<String> setConcepts) {
		for (OrderedPair<String> map : conceptCombination) {
			String leftConcept = map.getLeftElement();
			String rightConcept = map.getRightElement();
			if (setConcepts.contains(leftConcept) || setConcepts.contains(rightConcept))
				return true;
		}
		return false;
	}

	public static MapOfSet<String, StringEdge> createNameSpaceToEdgeSet(StringGraph inputSpace_) {
		MapOfSet<String, StringEdge> nameSpaceEdges = new MapOfSet<String, StringEdge>();
		Set<StringEdge> edgeSet = inputSpace_.edgeSet();
		for (StringEdge edge : edgeSet) {
			// String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();

			String sourceNS = getConceptNamespace(source);
			String targetNS = getConceptNamespace(target);

			// these two should be the same namespace but you never know
			nameSpaceEdges.put(sourceNS, edge);
			nameSpaceEdges.put(targetNS, edge);
		}
		// System.out.printf("createNameSpaceToEdgeSet() got %d namespaces :
		// %s\n", nameSpaceEdges.size(), nameSpaceEdges.keySet());
		return nameSpaceEdges;
	}

	public static MapOfSet<String, String> createNameSpaceToConceptSet(StringGraph inputSpace) {
		MapOfSet<String, String> nameSpaces = new MapOfSet<>();
		// scan for namespaces, stored as namespace/concept
		for (String concept : inputSpace.getVertexSet()) {
			String currentNameSpace = getConceptNamespace(concept);
			nameSpaces.put(currentNameSpace, concept);
		}
		// System.out.printf("createNameSpaceToConceptSet() got %d namespaces :
		// %s\n", nameSpaces.size(), nameSpaces.keySet());
		return nameSpaces;
	}

	public static MapOfList<OrderedPair<String>, OrderedPair<String>> createNeighboringMappings(Mapping<String> mappings, StringGraph graph) {
		MapOfList<String, OrderedPair<String>> conceptToContainingMappings = new MapOfList<>();
		for (OrderedPair<String> mapping : mappings.getOrderedPairs()) {
			List<String> concepts = mapping.getElements();
			for (String concept : concepts) {
				conceptToContainingMappings.put(concept, mapping);
			}
		}

		MapOfList<OrderedPair<String>, OrderedPair<String>> nearbyMappings = new MapOfList<>();
		for (OrderedPair<String> mapping : mappings.getOrderedPairs()) {
			List<OrderedPair<String>> nearby = getNearbyMappings(mapping, conceptToContainingMappings, graph);
			nearbyMappings.put(mapping, nearby);
		}
		return nearbyMappings;
	}

	/**
	 * Expands on the graph, from the openset, excluding nodes present in the closed set, returning the new expanded nodes. Whenever a new edge is being expanded from the current
	 * set to a neighboring node, ExpandingEdge ee is invoked.
	 *
	 * @param openSet
	 * @param closedSet
	 * @param graph
	 * @return
	 */
	public static Set<String> expandFromOpenSetOneLevel(Set<String> openSet, Set<String> closedSet, StringGraph graph, ExpandingEdge ee) {
		// changes to be done to open and closed sets (to prevent concurrent
		// modification exception)
		Set<String> openSetAddition = new HashSet<String>();
		Set<String> openSetRemoval = new HashSet<String>();
		// for each vertex in the open set not in the closed set
		for (String vertexId : openSet) {
			if (closedSet.contains(vertexId))
				continue;
			// get the vertex neighbors not in the closed set
			Set<String> neighbors = graph.getNeighborVertices(vertexId);
			for (String neighborId : neighbors) {
				if (closedSet.contains(neighborId))
					continue;
				// put the neighbors in the open set
				openSetAddition.add(neighborId);
				if (ee != null)
					ee.expanding(vertexId, neighborId);
			}
			// vertex from the open set explored, remove it from further
			// exploration
			openSetRemoval.add(vertexId);
			closedSet.add(vertexId);
		}
		// do the changes in the open and closed sets
		openSet.addAll(openSetAddition);
		openSet.removeAll(openSetRemoval);
		return openSetAddition;
	}

	/**
	 * Expands on the graph, from the openset, excluding nodes present in the closed set, returning the new expanded nodes. Whenever a new edge is being expanded from the current
	 * set to a neighboring node, ExpandingEdge ee is invoked.
	 *
	 * @param openSet
	 * @param closedSet
	 * @param graph
	 * @return
	 */
	public static Set<String> expandFromVertexOneLevel(String vertexId, Set<String> closedSet, StringGraph graph, ExpandingEdge ee) {
		// changes to be done to open and closed sets (to prevent concurrent
		// modification exception)
		Set<String> openSetAddition = new HashSet<String>();
		Set<String> openSetRemoval = new HashSet<String>();
		// not in the closed set
		if (!closedSet.contains(vertexId)) {
			// get the vertex neighbors not in the closed set
			Set<String> neighbors = graph.getNeighborVertices(vertexId);
			for (String neighborId : neighbors) {
				if (closedSet.contains(neighborId))
					continue;
				// put the neighbors in the open set
				openSetAddition.add(neighborId);
				if (ee != null)
					ee.expanding(vertexId, neighborId);
			}
			// vertex from the open set explored, remove it from further
			// exploration
			openSetRemoval.add(vertexId);
			closedSet.add(vertexId);
		}
		return openSetAddition;
	}

	public static Map<String, String> createConceptToNameSpaceMap(StringGraph genericSpace) {
		HashMap<String, String> conceptToNS = new HashMap<>();
		for (String concept : genericSpace.getVertexSet()) {
			String namespace = getConceptNamespace(concept);
			conceptToNS.put(concept, namespace);
		}
		return conceptToNS;
	}

	/**
	 * Creates a new graph whose vertices are contained in the given namespaces.
	 *
	 * @param graph
	 * @param namespaces
	 * @return
	 */
	public static StringGraph filterNamespaces(StringGraph graph, Collection<String> namespaces) {
		StringGraph out = new StringGraph();
		Set<StringEdge> edgeSet = graph.edgeSet();
		for (StringEdge edge : edgeSet) {
			String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();

			String sourceNS = getConceptNamespace(source);
			String targetNS = getConceptNamespace(target);
			if (namespaces.contains(sourceNS) && namespaces.contains(targetNS)) {
				out.addEdge(source, target, label);
				// System.out.printf("%s %s %s\n",source, target, label);
			}
		}
		return out;
	}

	public static Set<OrderedPair<String>> getAllMappingsFromAnalogies(List<Mapping<String>> analogies) {
		Set<OrderedPair<String>> allMappings = new HashSet<>();
		for (Mapping<String> analogy : analogies) {
			allMappings.addAll(analogy.getOrderedPairs());
		}
		return allMappings;
	}

	private static final HashMap<String, String> conceptToNamespace = new HashMap<>();
	private static final ReentrantLock rlock0 = new ReentrantLock();

	public static String getConceptNamespace(String concept) {
		String namespace = null;
		rlock0.lock();
		try {
			namespace = conceptToNamespace.get(concept);
		} finally {
			rlock0.unlock();
		}

		if (namespace != null)
			return namespace;

		int i0 = concept.indexOf('/');
		if (i0 < 0)
			return null;
		namespace = concept.substring(0, i0);

		rlock0.lock();
		try {
			conceptToNamespace.put(concept, namespace);
		} finally {
			rlock0.unlock();
		}
		return namespace;
	}

	private static final HashMap<String, String> fullNameToConcept = new HashMap<>();
	private static final ReentrantLock rlock1 = new ReentrantLock();

	public static String getConceptWithoutNamespace(String fullConcept) {
		String concept = null;
		rlock1.lock();
		try {
			concept = fullNameToConcept.get(fullConcept);
		} finally {
			rlock1.unlock();
		}

		if (concept != null)
			return concept;

		int i0 = fullConcept.indexOf('/');
		if (i0 < 0)
			return fullConcept;
		concept = fullConcept.substring(i0 + 1);

		rlock1.lock();
		try {
			fullNameToConcept.put(fullConcept, concept);
		} finally {
			rlock1.unlock();
		}
		return concept;
	}

	/**
	 * returns a random vertex from depth <limit> starting from the given vertex
	 *
	 * @param random
	 * @param startingVertex
	 * @param graph
	 * @param limit
	 * @return
	 */
	public static String getDeepRandomVertex(RandomGenerator random, String startingVertex, StringGraph graph, int limit) {
		String currentvertex = startingVertex;
		for (int i = 0; i < limit; i++) {
			Set<String> neighborhood = graph.getNeighborVertices(currentvertex);
			if (neighborhood.isEmpty()) {
				return currentvertex;
			}
			currentvertex = getRandomElementFromCollection(neighborhood, random);
		}
		return currentvertex;
	}

	public static String getVertexFromRandomWalk(RandomGenerator random, String startingVertex, StringGraph graph, int limit) {
		String currentvertex = startingVertex;
		for (int i = 0; i < limit; i++) {
			Set<StringEdge> edgesOf = graph.edgesOf(currentvertex);
			if (edgesOf.isEmpty()) {
				return currentvertex;
			}
			StringEdge nextEdge = getRandomElementFromCollection(edgesOf, random);
			currentvertex = nextEdge.getOppositeOf(currentvertex);
		}
		return currentvertex;
	}

	public static int getDistance(String startingVertex0, String startingVertex1, StringGraph graph) {
		// set of visited and to visit vertices for the expansion process
		// expand from vertex0 until arriving at vertex1
		Set<String> openSet = new HashSet<String>();
		Set<String> closedSet = new HashSet<String>();
		openSet.add(startingVertex0);

		int distance = 0;
		do {
			// only expand while there are vertices to expand
			if (openSet.size() == 0)
				break;
			if (openSet.contains(startingVertex1))
				break;
			expandFromOpenSetOneLevel(openSet, closedSet, graph, null);
			distance++;
		} while (true);
		return distance;
	}

	public static HashMap<String, ArrayList<StringEdge>> lowestCommonAncestorIsa(StringGraph graph, String vertexL, String vertexR, boolean useDerivedFrom, boolean useSynonym) {
		HashMap<String, StringEdge> cameFromEdgeL = new HashMap<>();
		HashMap<String, StringEdge> cameFromEdgeR = new HashMap<>();
		HashMap<String, ArrayList<StringEdge>> ancestors = new HashMap<>();

		// do the left-right breadth first expansion
		{
			HashSet<String> closedSetL = new HashSet<>();
			ArrayDeque<String> openSetL = new ArrayDeque<>();
			HashSet<String> touchedL = new HashSet<>();

			HashSet<String> closedSetR = new HashSet<>();
			ArrayDeque<String> openSetR = new ArrayDeque<>();
			HashSet<String> touchedR = new HashSet<>();

			openSetL.addLast(vertexL);
			openSetR.addLast(vertexR);
			while (openSetL.size() > 0 || openSetR.size() > 0) {
				{
					HashSet<String> expanded = lcaIsaRadialExpansion(graph, openSetL.removeFirst(), useDerivedFrom, useSynonym, cameFromEdgeL, closedSetL);
					openSetL.addAll(expanded);
					touchedL.addAll(expanded);
				}

				{
					HashSet<String> expanded = lcaIsaRadialExpansion(graph, openSetR.removeFirst(), useDerivedFrom, useSynonym, cameFromEdgeR, closedSetR);
					openSetR.addAll(expanded);
					touchedR.addAll(expanded);
				}
				HashSet<String> collision = intersection(touchedL, touchedR);
				if (collision.size() > 0) {
					System.currentTimeMillis();
					for (String ref : collision) {
						ArrayList<StringEdge> pathL = getEdgePath(ref, cameFromEdgeL);
						ArrayList<StringEdge> pathR = getEdgePath(ref, cameFromEdgeR);
						ArrayList<StringEdge> fullPath = new ArrayList<>(pathL.size() + pathR.size());
						Collections.reverse(pathL);
						fullPath.addAll(pathL);
						fullPath.addAll(pathR);
						// remove repeated edges
						// TODO: feels like a stupid solution
						fullPath = removeRepeatedEdges(fullPath);

						String ancestor = validateAndGetAncestorFromSequence(fullPath, vertexL, vertexR);
						if (ancestor != null) {
							ancestors.put(ancestor, fullPath);
						}
					}

					if (!ancestors.isEmpty()) {
						break;
					}
				}
			}
		}
		return ancestors;
	}

	public static ArrayList<StringEdge> removeRepeatedEdges(ArrayList<StringEdge> edges) {
		ArrayList<StringEdge> newEdges = new ArrayList<>(edges.size());
		HashSet<StringEdge> closedSet = new HashSet<>();
		for (StringEdge edge : edges) {
			if (closedSet.contains(edge))
				continue;
			closedSet.add(edge);
			newEdges.add(edge);
		}
		return newEdges;
	}

	/**
	 * @param fullPath [horse,isa,equinae, equinae,isa,animal, aves,isa,animal, bird,isa,aves]
	 * @param vertexL  horse
	 * @param vertexR  bird
	 * @return animal
	 */
	private static String validateAndGetAncestorFromSequence(ArrayList<StringEdge> fullPath, String vertexL, String vertexR) {
		HashSet<String> lastConcepts = new HashSet<>();
		lastConcepts.add(vertexL);

		boolean outgoingPhase = true;
		int reversedDirection = 0;
		String ancestor = null;

		// TODO: this fails with repeated edges
		for (StringEdge edge : fullPath) {
			String source = edge.getSource();
			String target = edge.getTarget();
			String relation = edge.getLabel();
			if (relation.equals("isa")) {

				// outgoing (to the right)
				if (outgoingPhase) {
					// still outgoing?
					if (lastConcepts.contains(source)) {
						lastConcepts.clear();
						lastConcepts.add(target);
					} else
					// changed direction?
					if (lastConcepts.contains(target)) {
						lastConcepts.clear();
						lastConcepts.add(source);
						outgoingPhase = false;
						reversedDirection++;
						ancestor = target;
					} else {
						System.err.println("TODO: 2795");
					}
				}

				// incoming (to the right)
				else {
					// still incoming?
					if (lastConcepts.contains(target)) {
						lastConcepts.clear();
						lastConcepts.add(source);
					} else
					// changed direction?
					if (lastConcepts.contains(source)) {
						lastConcepts.clear();
						lastConcepts.add(target);
						outgoingPhase = true;
						reversedDirection++;
					} else {
						System.err.println("TODO: 2796");
					}
				}

			} else {
				lastConcepts.add(source);
				lastConcepts.add(target);
			}
		}

		if (reversedDirection % 2 == 0 || !lastConcepts.contains(vertexR)) {
			return null;
		}
		return ancestor;
	}

	/**
	 * Path is REVERSED
	 * 
	 * @param endingConcept
	 * @param conceptCameFromEdge
	 * @return
	 */
	public static ArrayList<StringEdge> getEdgePath(String endingConcept, HashMap<String, StringEdge> conceptCameFromEdge) {
		HashSet<StringEdge> closedSet = new HashSet<>();
		ArrayList<StringEdge> path = new ArrayList<>();
		StringEdge source;
		String current = endingConcept;
		while (true) {
			source = conceptCameFromEdge.get(current);
			// System.out.println(current + "\t" + (source == null ? "[]" : source));
			if (source == null)
				break;
			if (!closedSet.contains(source)) {
				path.add(source);
			}
			closedSet.add(source);
			current = source.getOppositeOf(current);
		}
		return path;
	}

	/**
	 * used by function lowestCommonAncestorIsa()
	 * 
	 * @param graph
	 * @param currentVertex
	 * @param useDerivedFrom
	 * @param useSynonym
	 * @param cameFromEdge
	 * @param closedSetConcepts
	 * @return
	 */
	private static HashSet<String> lcaIsaRadialExpansion(StringGraph graph, String currentVertex, boolean useDerivedFrom, boolean useSynonym,
			HashMap<String, StringEdge> cameFromEdge, HashSet<String> closedSetConcepts) {
		HashSet<String> newOpenSet = new HashSet<>();
		// stop when left and right expansions collide
		// expand edges with vertices not in the closed set
		if (!closedSetConcepts.contains(currentVertex)) {
			closedSetConcepts.add(currentVertex);
			Set<StringEdge> edges = graph.edgesOf(currentVertex);
			for (StringEdge edge : edges) {
				String edgeLabel = edge.getLabel();
				if (edgeLabel.equals("isa") && edge.outgoesFrom(currentVertex) || // ISA are always outgoing
						edgeLabel.equals("derivedfrom") && useDerivedFrom || // DERIVEDFROM &
						edgeLabel.equals("synonym") && useSynonym) { // SYNONYM are bidirectional
					String neighbor = edge.getOppositeOf(currentVertex);
					if (closedSetConcepts.contains(neighbor))
						continue;

					if (!cameFromEdge.containsKey(neighbor)) {
						cameFromEdge.put(neighbor, edge);
					}
					// put the neighbors in the open set
					newOpenSet.add(neighbor);
				}
			}
		}
		return newOpenSet;
	}

	public static ArrayList<StringEdge> shortestIsaPath(StringGraph graph, String start, String toReach, boolean useDerivedFrom, boolean useSynonym) {
		HashMap<String, StringEdge> cameFromEdge = new HashMap<>();

		// TODO: closed and open sets should be for edges, not concepts (to allow further exploration using alternative paths)
		HashSet<String> closedSet = new HashSet<>();
		ArrayDeque<String> openSet = new ArrayDeque<>();
		openSet.addLast(start);

		while (!openSet.isEmpty()) {
			// get next vertex
			String currentVertex = openSet.removeFirst();
			// if we arrived at the destination, abort expansion
			// if (currentVertex.equals(toReach))
			// break;

			ArrayList<StringEdge> path = getEdgePath(currentVertex, cameFromEdge);
			// must contain at least one isa
			if (!path.isEmpty() && pathContainsConcept(toReach, path) && pathContainsConcept(toReach, path)) {
				Collections.reverse(path);
				return path;
			}

			// expand a vertex not in the closed set
			if (!closedSet.contains(currentVertex)) {
				// get the vertex neighbors not in the closed set

				Set<StringEdge> out = graph.edgesOf(currentVertex);
				for (StringEdge edge : out) {
					String neighborId = null;
					String label = edge.getLabel();
					if (label.equals("isa")) {
						neighborId = edge.getTarget();
					} else if (label.equals("derivedfrom") && useDerivedFrom) {
						neighborId = edge.getOppositeOf(currentVertex);
					} else if (label.equals("synonym") && useSynonym) {
						neighborId = edge.getOppositeOf(currentVertex);
					}
					if (neighborId == null || neighborId.equals(currentVertex) || closedSet.contains(neighborId))
						continue;
					if (!cameFromEdge.containsKey(neighborId)) {
						cameFromEdge.put(neighborId, edge);
					}
					// put the neighbors in the open set
					openSet.addLast(neighborId);
					// if (closedSet.contains(neighborId))
					// continue;
				}
				// vertex from the open set explored, remove it from further
				// exploration
				closedSet.add(currentVertex);
			}
		}
		return new ArrayList<>(1);
	}

	public static boolean pathContainsConcept(String concept, ArrayList<StringEdge> path) {
		for (StringEdge edge : path) {
			if (edge.containsConcept(concept)) {
				return true;
			}
		}
		return false;
	}

	public static boolean edgePathContainsISA(ArrayList<StringEdge> path) {
		boolean containsIsa = false;
		for (StringEdge edge : path) {
			if (edge.getLabel().equals("isa")) {
				containsIsa = true;
				break;
			}
		}
		return containsIsa;
	}

	/**
	 * TODO: dont know if this is 100% correct
	 * 
	 * @param graph
	 * @param start
	 * @param toReach
	 */
	public static void shortestPathSearch(StringGraph graph, String start, String toReach) {
		HashMap<String, String> cameFrom = new HashMap<>();

		HashSet<String> closedSet = new HashSet<>();
		ArrayDeque<String> openSet = new ArrayDeque<>();
		openSet.addLast(start);

		while (!openSet.isEmpty()) {
			// get next vertex
			String currentVertex = openSet.removeFirst();
			// if we arrived at the destination, abort expansion
			if (currentVertex.equals(toReach))
				break;
			// expand a vertex not in the closed set
			if (!closedSet.contains(currentVertex)) {
				// get the vertex neighbors not in the closed set
				// TODO: melhor getOutgoingVertices e caso falha, pesquisa ao contrario
				Set<String> neighbors = graph.getNeighborVertices(currentVertex);
				for (String neighborId : neighbors) {
					if (closedSet.contains(neighborId))
						continue;
					if (!cameFrom.containsKey(neighborId)) {
						cameFrom.put(neighborId, currentVertex);
					}
					// put the neighbors in the open set
					openSet.addLast(neighborId);
				}
				// vertex from the open set explored, remove it from further
				// exploration
				closedSet.add(currentVertex);
			}
		}

		// get path
		String source;
		String current = toReach;
		while (true) {
			source = cameFrom.get(current);
			System.out.println(current);
			System.out.println(graph.getBidirectedEdges(source, current));
			if (source == null)
				break;
			current = source;
		}
	}

	/**
	 * Returns sorted list of sets of concepts (components)
	 * 
	 * @param graph
	 * @return
	 */
	public static ListOfSet<String> extractGraphComponents(StringGraph graph) {
		ListOfSet<String> graphComponents = new ListOfSet<>();
		HashSet<String> potentialSet = new HashSet<>(graph.getVertexSet());
		while (potentialSet.size() > 0) {
			// just get a vertex
			String firstVertex = potentialSet.iterator().next();
			HashSet<String> closedSet = new HashSet<>();
			HashSet<String> openSet = new HashSet<>();
			// start in a given vertex
			openSet.add(firstVertex);
			// expand all neighbors
			// when it stops, you get an island
			while (openSet.size() > 0) {
				Set<String> newVertices = GraphAlgorithms.expandFromOpenSetOneLevel(openSet, closedSet, graph, null);
				if (newVertices.isEmpty())
					break;
				openSet.addAll(newVertices);
				openSet.removeAll(closedSet);
			}
			// one more component done
			graphComponents.add(closedSet);
			potentialSet.removeAll(closedSet);
		}
		graphComponents.sortList(false);
		return graphComponents;
	}

	/**
	 * removes all all components except the biggest one
	 * 
	 * @param graph
	 */
	public static void removeSmallerComponents(StringGraph graph) {
		if (graph.numberOfEdges() > 0) {
			return;
		} else {
			ListOfSet<String> components = extractGraphComponents(graph);
			graph.clear();
			graph.addEdges(graph, components.getSetAt(0));
		}
	}

	/**
	 * Extracts a connected set (component) from the given graph.
	 * 
	 * @param graph
	 * @param minNewConceptsTrigger
	 * @param minTotalConceptsTrigger
	 * @param random
	 * @return
	 */
	public static HashSet<String> extractRandomPart(StringGraph graph, int minNewConceptsTrigger, int minTotalConceptsTrigger, RandomGenerator random) {
		// just get a vertex
		String firstVertex = GraphAlgorithms.getRandomElementFromCollection(graph.getVertexSet(), random);
		HashSet<String> closedSet = new HashSet<>();
		HashSet<String> openSet = new HashSet<>();
		// start in a given vertex
		openSet.add(firstVertex);
		// ---
		while (openSet.size() > 0) {
			// do a radial expansion
			Set<String> newVertices = GraphAlgorithms.expandFromOpenSetOneLevel(openSet, closedSet, graph, null);
			if (newVertices.isEmpty())
				break;

			if (closedSet.size() > minTotalConceptsTrigger) {
				break;
			}

			if (newVertices.size() > minNewConceptsTrigger) {
				newVertices = GraphAlgorithms.randomSubSet(newVertices, minNewConceptsTrigger, random);
			}

			openSet.addAll(newVertices);
			openSet.removeAll(closedSet);
		}
		return closedSet;
	}

	public static <T> T getRandomElementFromCollection(Collection<T> collection, RandomGenerator random) {
		int size = collection.size();
		int index = random.nextInt(size);
		T obj = getElementFromCollection(collection, index);
		return obj;
	}

	public static <T> T getElementFromCollection(Collection<T> collection, int index) {
		if (collection instanceof List<?>) {
			List<T> asList = (List<T>) collection;
			T obj = asList.get(index);
			return obj;
		}

		// not a list, count iterations
		// int counter = 0;
		// for (T obj : collection) {
		// if (counter == index)
		// return obj;
		// counter++;
		// }
		// return null;

		Iterator<T> iter = collection.iterator();
		for (int j = 0; j < index; j++) {
			iter.next();
		}
		return iter.next();

	}

	public static List<OrderedPair<String>> getNearbyMappings(OrderedPair<String> mapping, MapOfList<String, OrderedPair<String>> conceptToContainingMappings, StringGraph graph) {
		HashSet<OrderedPair<String>> neighborMappings = new HashSet<>();
		List<String> mapConcepts = mapping.getElements();
		for (String concept : mapConcepts) {
			Set<String> conceptNeighbors = graph.getNeighborVertices(concept);
			for (String neighborConcept : conceptNeighbors) {
				List<OrderedPair<String>> maplist = conceptToContainingMappings.get(neighborConcept);
				if (maplist != null && !maplist.isEmpty()) {
					for (OrderedPair<String> neighborMapping : maplist) {
						neighborMappings.add(neighborMapping);
					}
				}
			}
		}
		return new ArrayList<>(neighborMappings);
	}

	public static Set<String> getNeighborhoodDepth(String from, int maxDepth, StringGraph graph) {
		Set<String> openSet = new HashSet<String>();
		Set<String> openSetRemoval = new HashSet<String>();
		Set<String> openSetAddition = new HashSet<String>();
		Set<String> closedSet = new HashSet<String>();
		openSet.addAll(graph.getNeighborVertices(from));
		closedSet.add(from);
		for (int currentDepth = 1; currentDepth < maxDepth; currentDepth++) {
			for (String vertexId : openSet) {
				if (closedSet.contains(vertexId))
					continue;
				Set<String> neighbors = graph.getNeighborVertices(vertexId);
				for (String neighborId : neighbors) {
					if (closedSet.contains(neighborId))
						continue;
					openSetAddition.add(neighborId);
				}
				openSetRemoval.add(vertexId);
				closedSet.add(vertexId);
			}
			openSet.addAll(openSetAddition);
			openSet.removeAll(openSetRemoval);
			openSetAddition.clear();
			openSetRemoval.clear();
		}
		return openSet;
	}

	public static double getRandomDoublePow(RandomGenerator random, double pow) {
		return FastMath.pow(random.nextDouble(), pow);
	}

	public static StringGraph intersectGraphWithVertexSet(StringGraph graph, Set<String> maskingVertexSet) {
		StringGraph graphCopy = new StringGraph(graph);
		// get list of vertices from graph
		Set<String> graphVertexSet = graph.getVertexSet();
		// iterate vertices from graph
		for (String graphVertex : graphVertexSet) {
			// for each graph vertice not in the mask set
			if (!maskingVertexSet.contains(graphVertex)) {
				// -remove vertex and associated edges from the graph
				graphCopy.removeVertex(graphVertex);
			}
		}
		return graphCopy;
	}

	/**
	 * returns the intersection of the given sets
	 *
	 * @param vertexSet0
	 * @param vertexSet1
	 * @return
	 */
	public static <T> HashSet<T> intersection(Collection<T> vertexSet0, Collection<T> vertexSet1) {
		HashSet<T> intersection = new HashSet<T>();
		for (T vertex0 : vertexSet0) {
			if (vertexSet1.contains(vertex0)) {
				intersection.add(vertex0);
			}
		}
		return intersection;
	}

	public static <T> Set<T> union(Collection<T> set0, Collection<T> set1) {
		HashSet<T> set = new HashSet<T>();
		set.addAll(set0);
		set.addAll(set1);
		return set;
	}

	/**
	 * returns true if both sets intersect
	 *
	 * @param vertexSet0
	 * @param vertexSet1
	 * @return
	 */
	public static <T> boolean intersects(Collection<T> vertexSet0, Collection<T> vertexSet1) {
		for (T vertex0 : vertexSet0) {
			if (vertexSet1.contains(vertex0)) {
				return true;
			}
		}
		return false;
	}

	public static <T> Set<T> randomSubSet(Collection<T> set, int numberOfElements, RandomGenerator random) {
		return randomSubSetOld(set, numberOfElements, random);
		// if (set instanceof RandomAccess) {
		// return randomSampleArrayList((ArrayList<T>) set, numberOfElements,
		// random);
		// } else {
		// return randomSampleSet(set, numberOfElements, random);
		// }
	}

	// my old version
	private static <T> Set<T> randomSubSetOld(Collection<T> set, int numberOfElements, RandomGenerator random) {
		ArrayList<T> list = new ArrayList<T>(set); // N
		shuffleArrayList(list, random); // N
		Set<T> randomSet = new HashSet<T>();
		for (int i = 0; i < numberOfElements; i++) { // N
			randomSet.add(list.get(i));
		}
		// ~= N + N + N ~= 3N
		return randomSet;
	}

	// taken from https://eyalsch.wordpress.com/2010/04/01/random-sample/
	// Floyd’s Algorithm
	public static <T> HashSet<T> randomSampleArrayList(ArrayList<T> array, int size, RandomGenerator random) {
		HashSet<T> set = new HashSet<T>(size);
		int n = array.size();
		for (int i = n - size; i < n; i++) {
			int pos = random.nextInt(i + 1);
			T item = array.get(pos);
			if (set.contains(item))
				set.add(array.get(i));
			else
				set.add(item);
		}
		return set;
	}

	// taken from https://eyalsch.wordpress.com/2010/04/01/random-sample/
	// Full Scan
	public static <T> HashSet<T> randomSampleSet(Collection<T> collection, int size, RandomGenerator random) {
		HashSet<T> set = new HashSet<T>(size);
		int visited = 0;
		Iterator<T> it = collection.iterator();
		while (size > 0) {
			T item = it.next();
			if (random.nextDouble() < ((double) size) / (collection.size() - visited)) {
				set.add(item);
				size--;
			}
			visited++;
		}
		return set;
	}

	// Implementing Fisher–Yates shuffle
	public static <T> void shuffleArrayList(ArrayList<T> array, RandomGenerator random) {
		for (int i = array.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			T element = array.get(j);
			array.set(j, array.get(i));
			array.set(i, element);
		}
	}

	public static ArrayList<String> splitConceptWithBar(String concept) {
		int bar_i = concept.indexOf('|');
		String concept0 = concept.substring(0, bar_i);
		String concept1 = concept.substring(bar_i + 1);
		ArrayList<String> split = new ArrayList<>(2);
		split.add(concept0);
		split.add(concept1);
		return split;
	}

	public static HashSet<String> getNameSpaces(StringGraph graph) {
		Set<String> concepts = graph.getVertexSet();
		HashSet<String> namespaces = new HashSet<>();
		for (String concept : concepts) {
			String namespace = GraphAlgorithms.getConceptNamespace(concept);
			if (!namespaces.contains(namespace)) {
				namespaces.add(namespace);
			}
		}
		return namespaces;
	}

	public static String getHighestDegreeVertex(Collection<String> vertexSet, StringGraph graph) {
		int highestDegree = -1;
		String highestDegreeConcept = null;
		for (String concept : vertexSet) {
			int degree = graph.degreeOf(concept);
			if (degree > highestDegree) {
				highestDegree = degree;
				highestDegreeConcept = concept;
			}
		}
		return highestDegreeConcept;
	}

	public static ArrayList<StringEdge> getEdgesWithSources(Collection<StringEdge> edges, Collection<String> collection) {
		ArrayList<StringEdge> inCommon = new ArrayList<>(edges.size());
		for (StringEdge edge : edges) {
			if (collection.contains(edge.getSource()))
				inCommon.add(edge);
		}
		return inCommon;
	}

	public static ArrayList<String> getEdgesSources(Collection<StringEdge> edges) {
		ArrayList<String> sources = new ArrayList<>(edges.size());
		for (StringEdge edge : edges) {
			sources.add(edge.getSource());
		}
		return sources;
	}

	public static HashSet<String> getEdgesSourcesAsSet(Collection<StringEdge> edges) {
		HashSet<String> sources = new HashSet<>(edges.size());
		for (StringEdge edge : edges) {
			sources.add(edge.getSource());
		}
		return sources;
	}

	public static ArrayList<String> getEdgesTargets(Collection<StringEdge> edges) {
		ArrayList<String> sources = new ArrayList<>(edges.size());
		for (StringEdge edge : edges) {
			sources.add(edge.getTarget());
		}
		return sources;
	}

	public static HashSet<String> getEdgesTargetsAsSet(Collection<StringEdge> edges) {
		HashSet<String> sources = new HashSet<>(edges.size());
		for (StringEdge edge : edges) {
			sources.add(edge.getTarget());
		}
		return sources;
	}

	public static ArrayList<String> getEdgesLabels(Collection<StringEdge> edges) {
		ArrayList<String> strings = new ArrayList<>(edges.size());
		for (StringEdge edge : edges) {
			strings.add(edge.getLabel());
		}
		return strings;
	}

	public static HashSet<String> getEdgesLabelsAsSet(Collection<StringEdge> edges) {
		HashSet<String> strings = new HashSet<>(edges.size());
		for (StringEdge edge : edges) {
			strings.add(edge.getLabel());
		}
		return strings;
	}

	public static <V, E> HashSet<E> getEdgesLabelsAsSet(Set<GraphEdge<V, E>> edges) {
		HashSet<E> strings = new HashSet<>(edges.size());
		for (GraphEdge<V, E> edge : edges) {
			strings.add(edge.getLabel());
		}
		return strings;
	}

	public static IntDirectedMultiGraph convertStringGraph2IntDirectedMultiGraph(StringGraph graph, // --
			ObjectIndex<String> vertexLabels, // --
			ObjectIndex<String> relationLabels) {
		IntDirectedMultiGraph out = new IntDirectedMultiGraph(1 << 20, 1 << 20, 1 << 20, 1 << 20);
		for (StringEdge edge : graph.edgeSet()) {
			int sourceId = vertexLabels.addObject(edge.getSource());
			int targetId = vertexLabels.addObject(edge.getTarget());
			int relationId = relationLabels.addObject(edge.getLabel());

			out.addEdge(sourceId, targetId, relationId);
		}
		return out;
	}

	public static StringGraph convertIntDirectedMultiGraph2StringGraph(IntDirectedMultiGraph graph, // --
			ObjectIndex<String> vertexLabels, // --
			ObjectIndex<String> relationLabels) {
		StringGraph out = new StringGraph(1 << 20, 1 << 20, 1 << 20, 1 << 20);
		for (IntGraphEdge edge : graph.edgeSet()) {
			String sourceId = vertexLabels.getObject(edge.getSource());
			String targetId = vertexLabels.getObject(edge.getTarget());
			String relationId = relationLabels.getObject(edge.getLabel());

			out.addEdge(sourceId, targetId, relationId);
		}
		return out;
	}

	public static Object2IntOpenHashMap<String> countRelations(StringGraph graph) {
		return countRelations(graph.edgeSet());
	}

	public static Object2IntOpenHashMap<String> countRelations(Set<StringEdge> edges) {
		Object2IntOpenHashMap<String> counter = new Object2IntOpenHashMap<>();
		for (StringEdge edge : edges) {
			String relation = edge.getLabel();
			int relationCount = counter.getInt(relation);
			counter.put(relation, ++relationCount);
		}
		return counter;
	}

	/**
	 * reads something of the sort "slave=X1,no_choice_or_freedom=X0,own=X2,master=X3" into a hashmap
	 * 
	 * @param string
	 * @return
	 */
	public static HashMap<String, String> readMap(String string) {
		HashMap<String, String> map = new HashMap<>();
		String[] pairs = string.split(",");
		for (String pair : pairs) {
			String[] elements = pair.split("=");
			map.put(elements[0], elements[1]);
		}
		return map;
	}

	/**
	 * from https://stackoverflow.com/a/13421319
	 * 
	 * @param array
	 * @return
	 */
	public static <T> ArrayList<T> arrayToArrayList(final T[] array) {
		int non_nullz = 0;
		for (final T s : array) {
			if (s != null) {
				non_nullz++;
			}
		}

		final ArrayList<T> l = new ArrayList<T>(non_nullz);

		for (final T s : array) {
			if (s != null) {
				l.add(s);
			}
		}
		return l;
	}
}
