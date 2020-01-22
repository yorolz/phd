package blender;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.commons.math3.random.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import blender.structures.AnalogySet;
import blender.structures.Mapping;
import genetic.GeneticAlgorithm;
import genetic.operators.FitnessEvaluator;
import genetic.operators.GeneInitializer;
import genetic.operators.GeneRepair;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import prolog.PatternFrameCombination;
import prolog.PrologUtils;

public class Blender {
	private static final String CSS = "css/graph.css";
	private static final int POPULATIONVIEWSIZE = 1;
	private static AtomicInteger fileCounter = new AtomicInteger();

	public static String generateFilenameWithTimestamp() {
		int fileID = fileCounter.getAndIncrement();
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date) + "_" + Integer.toString(fileID);
		return filename;
	}

	public static void main(String[] args) throws Exception {

		// System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		// System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		StringGraph genericSpace = new StringGraph();
		StringGraph inputSpace = new StringGraph();

		List<AnalogySet> analogies = AnalogySet
				.readMappingsTXT("C:\\Desktop\\bitbucket\\semantic graphs\\horse bird from francisco (original)\\horse_bird_mappings_franciscobook.txt");
		ArrayList<PatternFrameCombination> patternFrames = PrologUtils.createPatternFrames("pattern_frames.pl");
		ArrayList<Term> deltaFrames = PrologUtils.readClauseFile("delta_frames.pl");

		GraphReadWrite.readAutoDetect("C:\\Desktop\\bitbucket\\semantic graphs\\horse bird from francisco (original)\\horse_bird_from_book_with_namespaces.csv", inputSpace);

		// remove loops, as they may hang algorithms and/or prolog
		genericSpace.removeLoops();
		inputSpace.removeLoops();
		// pegasusSpace.removeLoops();

		// remove mappings with concepts not existing in the inputspace
		removeUnknownMappings(analogies, inputSpace);

		Blend.setupSharedVariables(inputSpace, analogies, genericSpace, patternFrames, deltaFrames);
		// Blend.setupUsefulness(pegasusSpace);

		Blender blr = new Blender();
		blr.execute();

	}

	private static void removeUnknownMappings(List<AnalogySet> analogies, StringGraph inputSpace) {
		Set<String> isConcepts = inputSpace.getVertexSet();
		for (AnalogySet analogy : analogies) {
			Set<Mapping<String>> mappings = analogy.getMappings();
			Iterator<Mapping<String>> iterator = mappings.iterator();
			while (iterator.hasNext()) {
				Mapping<String> mapping = iterator.next();
				// if inputspace does not contain both concepts from the mapping, the mapping is unknown
				if (!(isConcepts.contains(mapping.getLeftConcept()) && isConcepts.contains(mapping.getRightConcept()))) {
					System.out.printf("removing unknown mapping %s\n", mapping);
					iterator.remove();
				}
			}
		}
	}

	private double[] calculateDimensions(double n, ArrayList<Double> factors, double aspectRatio) {
		double[] dimensions = new double[2];
		double threshold = Math.sqrt(n) * aspectRatio;
		double a = 1;
		double b = 1;
		double lastA;
		boolean changed = false;
		for (int i = 0; i < factors.size(); i++) {
			double factor = factors.get(i);
			lastA = a;
			a *= factor;
			if (a >= threshold) {
				if (!changed) {
					a = lastA;
					changed = true;
				}
				b = b * factor;
			}
		}
		dimensions[0] = a;
		dimensions[1] = b;
		return dimensions;
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

	private void execute() throws Exception {
		GeneRepair<Blend> repairingOperator = new BlendRepair();

		FitnessEvaluator<Blend> fitnessEvaluator = new FitnessEvaluator<Blend>() {
			@Override
			public double[] evaluateFitness(Blend[] genes) {
				// single gene, which is the blend class
				double[] score;
				try {
					Blend blend = genes[0];
					score = blend.getScore();
					return score;
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1); // this is serious
				}
				return null;
			}
		};

		GeneInitializer<Blend> geneInitializer = new GeneInitializer<Blend>() {
			@Override
			public void initializeGenes(Blend[] genes, RandomGenerator random) {
				// single gene, which is the blend class
				genes[0] = new Blend(random);
				genes[0].initialize();
			}
		};

		GeneticAlgorithm<Blend> ga = new GeneticAlgorithm<>(Blend.class, new BlendMutation(), fitnessEvaluator, new BlendCrossover(), geneInitializer, 1);
		ga.setRepairingOperator(repairingOperator);
		ga.execute();
		Blend[] population = getGeneticAlgorithmPopulation(ga, POPULATIONVIEWSIZE);
		// System.gc();
		visualizePopulation(population);
	}

	public double[][] getMdFitness(Blend[] population) {
		int populationSize = population.length;
		double[][] scores = new double[populationSize][4];
		for (int i = 0; i < populationSize; i++) {
			Blend b = population[i];
			// System.out.printf("%f\t%f\n", b.topologyMean, b.oneLevelEntropy);
			scores[i][0] = b.topologyMean;
			scores[i][1] = b.topologyStdDev;
			scores[i][2] = b.oneLevelEntropy;
			scores[i][3] = b.twoLevelEntropy;
		}
		return scores;
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

	private Blend[] getGeneticAlgorithmPopulation(GeneticAlgorithm<Blend> ga, int amount) {
		int populationSize = ga.getPopulationSize();
		Blend[] population = new Blend[amount];
		for (int i = 0; i < amount; i++) {
			// ga population is in ascending order, revert so that the first element is the highest
			Blend[] b = ga.getGenes(populationSize - i - 1);
			population[i] = b[0];
		}
		return population;
	}

	@SuppressWarnings("serial")
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
		//ArrayList<String> scoreKeys = sortScoreKeys(population);

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

	@SuppressWarnings("unused")
	private ArrayList<String> sortScoreKeys(Blend[] population) {
		ArrayList<String> scoreKeys = new ArrayList<String>();
		Blend blend0 = population[0];
		Set<String> scoreMapKeySet = blend0.getScoreMapKeySet();
		scoreKeys.addAll(scoreMapKeySet);
		Collections.sort(scoreKeys);
		return scoreKeys;
	}

}
