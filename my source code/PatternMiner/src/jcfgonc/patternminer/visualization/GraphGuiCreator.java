package jcfgonc.patternminer.visualization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import graph.StringEdge;
import graph.StringGraph;

public class GraphGuiCreator {

	private static String CSS = "css/graph.css";

	private static Node addNodeToGraph(MultiGraph visualGraph, String nodeLabel) {
		Node node = visualGraph.getNode(nodeLabel);
		if (node == null) {
			node = visualGraph.addNode(nodeLabel);
			node.addAttribute("ui.label", nodeLabel);
//			if (nodeLabel.contains("|")) {
//				node.addAttribute("ui.class", "blend");
//			}
		}
		return node;
	}

	public static MultiGraph createGraph(StringGraph graph) {
		MultiGraph visualGraph = new MultiGraph("output space");

		String styleSheet = null;
		try {
			styleSheet = new String(Files.readAllBytes(Paths.get(CSS)));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		visualGraph.addAttribute("ui.stylesheet", styleSheet);
		visualGraph.addAttribute("ui.quality");
		visualGraph.addAttribute("ui.antialias");

		Set<StringEdge> edgeSet = graph.edgeSet();
		for (StringEdge edge : edgeSet) {
			String edgeSource = edge.getSource();
			String edgeTarget = edge.getTarget();
			String edgeLabel = edge.getLabel();

			Node sourceNode = addNodeToGraph(visualGraph, edgeSource);
			Node targetNode = addNodeToGraph(visualGraph, edgeTarget);

			Edge addEdge = visualGraph.addEdge(edge.toString(), sourceNode, targetNode, true);
			addEdge.addAttribute("ui.label", edgeLabel);

//			if (Blend.isInterspaceEdge(edge)) {
//				// if (Blend.interspaceEdge(edge, conceptNameSpaces)) {
//				addEdge.addAttribute("ui.class", "red");
//			}
		}

		return visualGraph;
	}
}
