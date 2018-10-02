package jcfgonc.ws.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.Gson;

import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;

public class JsonGraphHandler {
	private static Gson gson = new Gson();

	public static StringGraph fromJson(String json) {
		JsonGraph jgraph = gson.fromJson(json, JsonGraph.class);

		ObjectIndex<String> vertexIDs = new ObjectIndex<>();
		StringGraph graph = new StringGraph();
		// addvertex was removed
		// int counter = 0;
		// for (Node node : jgraph.nodes) {
		// String label = node.label;
		// vertexIDs.addObject(label, counter);
		// graph.addVertex(label);
		// counter++;
		// }
		for (Link edge : jgraph.links) {
			String label = edge.label;
			String source = vertexIDs.getObject(edge.source);
			String target = vertexIDs.getObject(edge.target);
			StringEdge se = new StringEdge(source, target, label);
			graph.addEdge(se);
		}
		return graph;
	}

	public static String readFileToString(String filename) throws IOException {
		Path path = Paths.get(filename);
		byte[] bytes = Files.readAllBytes(path);
		String contents = new String(bytes);
		return contents;
	}

	// in this json every relation/edge is a node
	public static String toJson(StringGraph sg) {
		JsonGraph jg = new JsonGraph();
		// burocracies
		jg.directed = true;
		jg.multigraph = true;
		jg.graph = new ArrayList<>();
		ArrayList<String> inner = new ArrayList<>();
		inner.add("name");
		inner.add("0");
		jg.graph.add(inner);

		jg.nodes = new ArrayList<>();
		ObjectIndex<String> nodeIDs = new ObjectIndex<>();
		for (String concept : sg.getVertexSet()) {
			@SuppressWarnings("unused")
			int vid = nodeIDs.addObject(concept); // that vid...
			Node n = new Node();
			n.id = concept; // laughable... not the vid but instead it is the text label itself?
			n.label = concept; // lol again
			jg.nodes.add(n);
		}

		// // because edge labels are also nodes... or not (don't know)
		// for (StringEdge se : sg.edgeSet()) {
		// String relation = se.getLabel();
		// if (relation.isEmpty())
		// continue;
		// if (nodeIDs.containsObject(relation))
		// continue;
		// @SuppressWarnings("unused")
		// int vid = nodeIDs.addObject(relation); // that vid...
		// Node n = new Node();
		// n.id = relation;
		// n.label = relation;
		// jg.nodes.add(n);
		// }

		jg.links = new ArrayList<>();
		for (StringEdge se : sg.edgeSet()) {
			int sourceID = nodeIDs.getObjectId(se.getSource());
			int targetID = nodeIDs.getObjectId(se.getTarget());
			String edgeLabel = se.getLabel();
			// link source to target
			jg.links.add(new Link(sourceID, targetID, 0, edgeLabel));// or 1, don't know what that means
		}

		String json = gson.toJson(jg);
		return json;
	}

}
