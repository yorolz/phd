package jcfgonc.patternminer.visualization;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;

public class GraphVisualizer {

	private static String CSS = "css/graph.css";

	public static StringGraph createGraphFromString(String string) throws NoSuchFileException, IOException {
		string = string.replaceAll("\r\n", ""); // remove lines
		string = string.replaceAll(";", "\r\n"); // convert ; to lines
		// System.out.format("query is:\n%s\n", query);
		StringGraph graph = new StringGraph();
		StringReader sr = new StringReader(string);
		GraphReadWrite.readCSV(sr, graph);
		sr.close();
		return graph;
	}

	private static Node addNodeToViewer(MultiGraph visualGraph, String nodeLabel) {
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

	public static DefaultView createGraphViewer(StringGraph graph) {
		ObjectIndex<String> edgeDictionary = new ObjectIndex<String>();
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

			Node sourceNode = addNodeToViewer(visualGraph, edgeSource);
			Node targetNode = addNodeToViewer(visualGraph, edgeTarget);

			int edgeId = edgeDictionary.addObject(edgeLabel);
			Edge addEdge = visualGraph.addEdge(Integer.toString(edgeId), sourceNode, targetNode, true);
			addEdge.addAttribute("ui.label", edgeLabel);

//			if (Blend.isInterspaceEdge(edge)) {
//				// if (Blend.interspaceEdge(edge, conceptNameSpaces)) {
//				addEdge.addAttribute("ui.class", "red");
//			}
		}

		Viewer viewer = new Viewer(visualGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout(Layouts.newLayoutAlgorithm());
		DefaultView view = (DefaultView) viewer.addDefaultView(false);
		view.setMinimumSize(new Dimension(64, 64));
		return view;
	}
}
