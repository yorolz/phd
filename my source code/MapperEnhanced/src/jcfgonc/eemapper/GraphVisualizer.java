package jcfgonc.eemapper;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import blender.Blend;
import graph.StringEdge;
import graph.StringGraph;

public class GraphVisualizer {
	@SuppressWarnings("unused")
	private ArrayList<String> sortScoreKeys(Blend[] population) {
		ArrayList<String> scoreKeys = new ArrayList<String>();
		Blend blend0 = population[0];
		Set<String> scoreMapKeySet = blend0.getScoreMapKeySet();
		scoreKeys.addAll(scoreMapKeySet);
		Collections.sort(scoreKeys);
		return scoreKeys;
	}

	public ArrayList<Double> factor(int n) {
		ArrayList<Double> factors = new ArrayList<>();

		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				factors.add((double) i);
				n = n / i;
			}
		}
		return factors;
	}

	private Node addNodeToViewer(MultiGraph visualGraph, String nodeLabel) {
		Node node = visualGraph.getNode(nodeLabel);
		if (node == null) {
			node = visualGraph.addNode(nodeLabel);
			node.addAttribute("ui.label", nodeLabel);
			if (nodeLabel.contains("|")) {
				node.addAttribute("ui.class", "blend");
			}
		}
		return node;
	}

	private void visualizePopulation(Blend[] population) throws Exception {
		JFrame mainFrame = new JFrame();
		Container framePane = mainFrame.getContentPane();
		JRootPane rootPane = mainFrame.getRootPane();
		int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
		rootPane.getInputMap(IFW).put(KeyStroke.getKeyStroke("ESCAPE"), "terminate");
		rootPane.getActionMap().put("terminate", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		int populationSize = population.length;
		ArrayList<Double> factors = factor(populationSize);
		double aspectRatio = 16.0 / 9.0;
		double[] wh = calculateDimensions(populationSize, factors, aspectRatio);

		GridLayout layout = new GridLayout((int) wh[1], (int) wh[0]);
		framePane.setLayout(layout);
		mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// cache score keys order
		// ArrayList<String> scoreKeys = sortScoreKeys(population);

		for (int i = 0; i < populationSize; i++) {
			Blend blend = population[i];

			// save graph to a file
			// String filename = "blend_" + generateFilenameWithTimestamp() + "___" + i;
			// GraphReadWrite.writeTGF(filename + ".tgf", blend.outputSpace);
			// GraphReadWrite.writeDT(filename + ".dt", blend.outputSpace, "blend");

			MultiGraph graph = createGraphViewer(blend);
			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

			Layout graphLayout = Layouts.newLayoutAlgorithm();
			viewer.enableAutoLayout(graphLayout);

			DefaultView view = (DefaultView) viewer.addDefaultView(false);
			JInternalFrame jif = new JInternalFrame("blend[" + Integer.toString(i) + "]");
			jif.add(view);
			framePane.add(jif);
			jif.setBorder(BorderFactory.createLineBorder(Color.black));
			// jif.pack();
			jif.setVisible(true);

			// TODO: key for toggling this?
			// view.setBackLayerRenderer(new LayerRenderer() {
			// @Override
			// public void render(Graphics2D graphics, GraphicGraph graph, double px2Gu, int widthPx, int heightPx, double minXGu,
			// double minYGu, double maxXGu, double maxYGu) {
			// int nConcepts = blend.outputSpace.numberOfVertices();
			// int nRelations = blend.outputSpace.edgeSet().size();
			// graphics.setColor(Color.BLACK);
			// graphics.setFont(new Font("Consolas", Font.PLAIN, 20));
			// int ypos = 16;
			//
			// graphics.drawString("nConcepts: " + nConcepts, 4, ypos);
			// ypos += 20;
			// graphics.drawString("nRelations: " + nRelations, 4, ypos);
			// ypos += 20;
			//
			// graphics.drawString("score: " + Arrays.toString(blend.getScore()), 4, ypos);
			// ypos += 20;
			//
			// for (String scoreKey : scoreKeys) {
			// String scoree = blend.getScoreForEntry(scoreKey);
			// graphics.drawString(scoreKey + ": " + scoree, 4, ypos);
			// ypos += 20;
			// }
			// }
			// });
		}

		// Display the window.
		// mainFrame.pack();
		mainFrame.setVisible(true);
	}

	private MultiGraph createGraphViewer(Blend blend) {
		StringGraph blendGraph = blend.outputSpace;

		MultiGraph visualGraph = new MultiGraph("output space");
		ArrayList<String> edgeLabels = new ArrayList<>();

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

		Set<StringEdge> edgeSet = blendGraph.edgeSet();
		for (StringEdge edge : edgeSet) {
			String edgeSource = edge.getSource();
			String edgeTarget = edge.getTarget();
			String edgeLabel = edge.getLabel();

			Node sourceNode = addNodeToViewer(visualGraph, edgeSource);
			Node targetNode = addNodeToViewer(visualGraph, edgeTarget);

			int edgeIndex = edgeLabels.size();
			edgeLabels.add(edgeLabel);
			Edge addEdge = visualGraph.addEdge(Integer.toString(edgeIndex), sourceNode, targetNode, true);
			addEdge.addAttribute("ui.label", edgeLabel);

			if (Blend.isInterspaceEdge(edge)) {
				// if (Blend.interspaceEdge(edge, conceptNameSpaces)) {
				addEdge.addAttribute("ui.class", "red");
			}
		}

		// Viewer display = visualGraph.display();
		return visualGraph;
	}
}
