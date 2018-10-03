package matcher;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import graph.EdgeDirection;
import graph.StringEdge;
import graph.StringGraph;
import structures.MapOfSet;

public class MatchingDepthStep {
	public static boolean debug = Config.DEBUG;

	private static Set<Mapping<String>> createAnalogy(String leftVertex, String rightVertex, HashMap<String, Integer> vertexDeepness,
			MapOfSet<String, String> leftToRightCorrespondences, HashMap<String, String> leftAncestor, MatchingBreadthStep breadth, boolean writeCombinationsFile)
			throws IOException {

		Mapping<String> baseMapping = new Mapping<String>(leftVertex, rightVertex);
		ArrayList<Mapping<String>> root = new ArrayList<>();
		root.add(baseMapping);

		// TODO: accelerate this!
		Set<Mapping<String>> analogy = AnalogyExtractor.execute(root, vertexDeepness, leftToRightCorrespondences, leftAncestor, breadth, writeCombinationsFile);
		return analogy;
	}

	public static Set<Mapping<String>> execute(StringGraph graph, String leftVertex, String rightVertex, boolean writeCombinationsFile) throws IOException {
		if (debug)
			System.out.println(new Throwable().getStackTrace()[0].toString());

		ArrayDeque<String> queue = new ArrayDeque<>();
		HashSet<String> closedSet = new HashSet<>();
		HashMap<String, Integer> vertexDeepness = new HashMap<>(); // a vertex only has one deepness level
		MapOfSet<String, String> leftToRightCorrespondences = new MapOfSet<>(); // valid correspondences (mappings) from left to right vertices
		HashMap<String, String> leftAncestor = new HashMap<>();
		HashSet<String> leftConcepts = new HashSet<>();
		leftToRightCorrespondences.put(leftVertex, rightVertex);

		MatchingBreadthStep breadth = new MatchingBreadthStep(graph, rightVertex);

		queue.push(leftVertex);
		vertexDeepness.put(leftVertex, 0);

		// ---------------- left queue loop
		while (!queue.isEmpty()) {
			step(graph, queue, closedSet, vertexDeepness, breadth, leftToRightCorrespondences, leftAncestor, leftConcepts);
			if (StaticTimer.timedOut())
				break;
		}

		Set<Mapping<String>> analogy = createAnalogy(leftVertex, rightVertex, vertexDeepness, leftToRightCorrespondences, leftAncestor, breadth, writeCombinationsFile);
		return analogy;
	}

	private static void step(StringGraph graph, ArrayDeque<String> queue, HashSet<String> closedSet, HashMap<String, Integer> vertexDeepness, MatchingBreadthStep breadth,
			MapOfSet<String, String> leftToRightCorrespondences, HashMap<String, String> ancestor, HashSet<String> leftVisitedConcepts) {
		// left expands depth first
		// right expands breadth first, in synchrony with left position
		// left starts at given left vertex
		// this is cut/aborted as soon as there is no way to map to the other side
		if (debug)
			System.out.println("----------\nleftVertexQueue:" + queue);
		String currentVertex = queue.pop();
		int currentDeepness = vertexDeepness.get(currentVertex);
		leftVisitedConcepts.add(currentVertex);

		if (!closedSet.contains(currentVertex)) {
			if (debug)
				System.out.println("leftVertex:" + currentVertex + " deep:" + currentDeepness);

			closedSet.add(currentVertex);
			// expand relation edge in left

			Set<StringEdge> expandedEdges = graph.edgesOf(currentVertex);
			if (!expandedEdges.isEmpty()) {

				currentDeepness = currentDeepness + 1;
				// update vertex set in the right graph at the current deepness
				breadth.updateDepthSets(currentDeepness, leftVisitedConcepts);
				HashSet<String> rightVisitedConcepts = breadth.getRightVisitedConcepts();

				for (StringEdge expandedEdge : expandedEdges) {
					// -------------
					String expandedNeighbor = expandedEdge.getOppositeOf(currentVertex);

					// we may be going backwards
					if (closedSet.contains(expandedNeighbor))
						continue;
					// was expanded before but not yet visited
					if (ancestor.containsKey(expandedNeighbor))
						continue;
					// we may be colliding with the right set
					if (rightVisitedConcepts.contains(expandedNeighbor))
						continue;

					// store neighbor level and precedence
					vertexDeepness.put(expandedNeighbor, currentDeepness);
					ancestor.put(expandedNeighbor, currentVertex);
					leftVisitedConcepts.add(expandedNeighbor);

					String relation = expandedEdge.getLabel();
					EdgeDirection edgeDirection = StringGraph.getEdgeDirectionRelativeTo(expandedNeighbor, expandedEdge);

					// get correspondences from the right side
					// which will serve as the ancestors of the current expanded neighbors
					Set<String> rightMatchAncestor = leftToRightCorrespondences.get(currentVertex);
					Set<String> rightMatches = breadth.getVertices(currentDeepness, relation, rightMatchAncestor, edgeDirection);

					// if there are not mappings on the other side, this vertex has no reason to be explored
					if (rightMatches.isEmpty()) {
						if (debug)
							System.out.println("leftVertex:" + expandedNeighbor + " will not be explored");
						continue;
					}

					queue.push(expandedNeighbor);
					leftToRightCorrespondences.put(expandedNeighbor, rightMatches);

					if (debug)
						System.out.println("mappings:" + expandedNeighbor + " to " + rightMatches);
					if (debug)
						System.out.println("\tleftEdge:" + relation + "\t->:" + expandedNeighbor);
					
					// check for timeout
					if (StaticTimer.timedOut())
						break;
				}
			} else {

				if (debug)
					System.out.println("###########");

			}
		}
	}
}
