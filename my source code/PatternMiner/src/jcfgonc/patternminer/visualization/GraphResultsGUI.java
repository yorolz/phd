package jcfgonc.patternminer.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;

import utils.OSTools;

public class GraphResultsGUI extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int GRAPH_SIZE_MAXIMUM = 2048;
	private static final int GRAPH_SIZE_MINIMUM = 100;
	private static final String graphDatafile = "mergedResults.csv";
	private static final int default_graphSize = 200;
	private static final int DEFAULT_FONT_SIZE = 18;
	private static final int NODE_SIZE_MINIMUM = 0;
	private static final int NODE_SIZE_MAXIMUM = 100;
	private static final int DEFAULT_NODE_SIZE = 24;

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	public static void main(String[] args) throws NoSuchFileException, IOException {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphResultsGUI frame = new GraphResultsGUI();
					frame.initializeTheRest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel contentPane;
	private JPanel graphPanel;
	private JScrollPane scrollPane;
	private JPanel settingsPanel;
	private JSlider graphSizeSlider;
	private JSplitPane horizontalPanel;
	private JPanel graphSizePanel;
	private JLabel graphSizeLabel;
	private int graphSize = default_graphSize;
	private int graphFontSize = DEFAULT_FONT_SIZE;
	private int graphNodeSize = DEFAULT_NODE_SIZE;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontSizeLabel;
	private HashMap<String, GraphData> graphMap;
	private ArrayList<GraphData> graphList;
	private JPanel nodeSizePanel;
	private JSlider nodeSizeSlider;
	private JLabel nodeSizeLabel;
	private JPanel renderingControlPanel;
	private JPanel searchPanel;
	private JCheckBox ascendingCB;
	private JComboBox<String> sortingColumnBox;
	private JCheckBox varRenderCB;
	private boolean sortAscending;

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public GraphResultsGUI() throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		initialize();
	}

	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		horizontalPanel = new JSplitPane();
		contentPane.add(horizontalPanel, BorderLayout.CENTER);
		horizontalPanel.setEnabled(true);
		horizontalPanel.setContinuousLayout(true);
		horizontalPanel.setResizeWeight(1.0);
		horizontalPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				windowResized();
			}
		});

		graphPanel = new JPanel();
		scrollPane = new JScrollPane(graphPanel);
		scrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		horizontalPanel.setLeftComponent(scrollPane);
		graphPanel.setBorder(null);
		graphPanel.setLayout(new GridLayout());
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateGraphVisibility(e);
			}
		});

		settingsPanel = new JPanel();
		settingsPanel.setMinimumSize(new Dimension(224, 10));
		horizontalPanel.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		renderingControlPanel = new JPanel();
		renderingControlPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Rendering",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(renderingControlPanel);
		renderingControlPanel.setLayout(new BoxLayout(renderingControlPanel, BoxLayout.Y_AXIS));

		graphSizePanel = new JPanel();
		renderingControlPanel.add(graphSizePanel);
		graphSizePanel.setBorder(new TitledBorder(null, "Graph Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		graphSizeSlider = new JSlider(GRAPH_SIZE_MINIMUM, GRAPH_SIZE_MAXIMUM, default_graphSize);
		graphSizeSlider.setPaintLabels(true);
		graphSizePanel.add(graphSizeSlider);
		graphSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateGraphsSizeControl(source);
			}
		});

		graphSizeLabel = new JLabel(Integer.toString(graphSize));
		graphSizePanel.add(graphSizeLabel);

		fontScalePanel = new JPanel();
		renderingControlPanel.add(fontScalePanel);
		fontScalePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Font Size", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));

		fontScaleSlider = new JSlider(FONT_SIZE_MINIMUM, FONT_SIZE_MAXIMUM, DEFAULT_FONT_SIZE);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(DEFAULT_FONT_SIZE);
				}
			}
		});
		fontScaleSlider.setPaintLabels(true);
		fontScalePanel.add(fontScaleSlider);
		fontScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateGraphFontsSizeControl(source);
			}
		});

		fontSizeLabel = new JLabel(Integer.toString(graphFontSize));
		fontScalePanel.add(fontSizeLabel);

		nodeSizePanel = new JPanel();
		renderingControlPanel.add(nodeSizePanel);
		nodeSizePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Node Size", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));

		nodeSizeSlider = new JSlider(NODE_SIZE_MINIMUM, NODE_SIZE_MAXIMUM, DEFAULT_NODE_SIZE);
		nodeSizeSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					nodeSizeSlider.setValue(DEFAULT_NODE_SIZE);
				}
			}
		});
		nodeSizeSlider.setPaintLabels(true);
		nodeSizePanel.add(nodeSizeSlider);
		nodeSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateGraphSizeControl(source);
			}
		});

		nodeSizeLabel = new JLabel(Integer.toString(graphNodeSize));
		nodeSizePanel.add(nodeSizeLabel);

		varRenderCB = new JCheckBox("Toggle Vertex Labelling");
		varRenderCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				toggleGraphMode(e);
			}
		});
		renderingControlPanel.add(varRenderCB);

		searchPanel = new JPanel();
		searchPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Graph Sorting", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(searchPanel);

		ascendingCB = new JCheckBox("Ascending");
		ascendingCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				sortAscending = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
		ascendingCB.setHorizontalAlignment(SwingConstants.CENTER);
		searchPanel.add(ascendingCB);

		sortingColumnBox = new JComboBox<>();
		sortingColumnBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				String columnName = (String) cb.getSelectedItem();
				System.out.println(columnName);
			}
		});
		searchPanel.add(sortingColumnBox);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized();
			}
		});

	}

	private void toggleGraphMode(ItemEvent e) {
		boolean alternativeLabelling = false;
		switch (e.getStateChange()) {
		case ItemEvent.SELECTED:
			alternativeLabelling = true;
			break;
		case ItemEvent.DESELECTED:
			alternativeLabelling = false;
			break;
		}
		for (GraphData gd : graphList) {
			gd.changeGraphVertexLabelling(alternativeLabelling);
		}
	}

	private void updateGraphVisibility(ChangeEvent e) {
		for (GraphData gd : graphList) {
			// gd.getViewer().disableAutoLayout();
			DefaultView defaultView = gd.getDefaultView();
			boolean isVisible = !defaultView.getVisibleRect().isEmpty();
			defaultView.setEnabled(isVisible);
			defaultView.setVisible(isVisible);
//			if (isVisible) {
//				System.out.print(1);
//			} else {
//				System.out.print(0);
//			}
		}
//		System.out.println();
	}

	private void windowResized() {
		setupGraphPanellayout();
	}

	public void initializeTheRest() throws NoSuchFileException, IOException {
		double w = 640 * OSTools.getScreenScale();
		double h = 480 * OSTools.getScreenScale();
		setSize(new Dimension((int) w, (int) h));
		sortAscending = false;
		createGraphs();
		// addGraphsToPanel();
		if (!graphList.isEmpty()) {
			GraphData gd = graphList.get(0);
			String[] columnNames = gd.getDetailsHeader().toArray(new String[0]);
			sortingColumnBox.setModel(new DefaultComboBoxModel<String>(columnNames));
		}
//		updateGraphFontsSizeControl(null);
//		updateGraphSizeControl(null);
//		updateGraphsSizeControl(null);
//		pack();
		setLocationRelativeTo(null);
	}

	@SuppressWarnings("unused")
	private void graphAutoLayoutTimeout() {
		Thread timeOutThread = new Thread() {
			public void run() {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
				for (GraphData gd : graphMap.values()) {
					gd.getViewer().disableAutoLayout();
				}
			}
		};
		timeOutThread.start();
	}

	private void createGraphs() throws FileNotFoundException, IOException, NoSuchFileException {
		graphMap = new HashMap<>();
		graphList = new ArrayList<>();

		ArrayList<GraphData> gds = GraphData.createGraphsFromCSV("\t", new File(graphDatafile), true, graphSize);
		if (gds.isEmpty())
			return;
		for (GraphData gd : gds) {
			DefaultView defaultView = gd.getDefaultView();
//			DefaultMouseManager manager = new DefaultMouseManager();
//			defaultView.setMouseManager(manager);
//			manager.release();

			defaultView.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						String clickedComponent = e.getComponent().getName();
						GraphData clickedGD = graphMap.get(clickedComponent);
						clickedGD.toggleSelected();
					}
				}
			});
			graphMap.put(gd.getId(), gd);
			graphList.add(gd);
			graphPanel.add(defaultView);
		}

//		graphAutoLayoutTimeout();
		updateFontsSize();
		updateNodesSize();
	}

	private void setupGraphPanellayout() {
		// TODO: check for a low number of graphs, in that case that number should be the
		GridLayout layout = (GridLayout) graphPanel.getLayout();
		int graphsPerColumn = (int) Math.floor(scrollPane.getViewport().getWidth() / graphSize);
		if (layout.getColumns() != graphsPerColumn) { // just to optimize the layout
			if (graphsPerColumn >= 1 && graphsPerColumn <= 8) {
				if (graphList.size() < graphsPerColumn) {
					graphsPerColumn = graphList.size();
				}
				layout.setColumns(graphsPerColumn);
				layout.setRows(0);
			}
			graphPanel.revalidate();
		}
//		System.out.printf("graphsPerColumn\t%d\n", graphsPerColumn);
	}

	private void updateFontsSize() {
		for (GraphData gd : graphList) {
			MultiGraph graph = gd.getMultiGraph();
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			graph.addAttribute("ui.stylesheet", style);
		}
	}

	private void updateGraphsSizeControl(JSlider source) {
		if (source != null) {
			graphSize = source.getValue();
		}
		graphSizeLabel.setText(Integer.toString(graphSize));
//		if (source.getValueIsAdjusting())
//			return;
		for (GraphData gd : graphList) {
			DefaultView view = gd.getDefaultView();
			// both these set*Size and later a validate/revalidate must be called to force the flowlayout to "relayout" its components
			Dimension size = new Dimension(graphSize, graphSize);
			view.setMinimumSize(size);
			view.setMaximumSize(size);
			view.setPreferredSize(size);
			view.setSize(size);
		}
		setupGraphPanellayout();
	}

	private void updateGraphFontsSizeControl(JSlider source) {
		if (source != null) {
			graphFontSize = source.getValue();
		}
		fontSizeLabel.setText(Integer.toString(graphFontSize));
		updateFontsSize();
	}

	private void updateGraphSizeControl(JSlider source) {
		if (source != null) {
			graphNodeSize = source.getValue();
		}
		nodeSizeLabel.setText(Integer.toString(graphNodeSize));
		updateNodesSize();
	}

	private void updateNodesSize() {
		for (GraphData gd : graphList) {
			MultiGraph graph = gd.getMultiGraph();
			String style;
			if (graphNodeSize == 0) {
				style = String.format("node { stroke-mode: none; }");
			} else {
				style = String.format("node { stroke-mode: plain; size: %dpx; }", graphNodeSize, graphNodeSize);
			}
			graph.addAttribute("ui.stylesheet", style);
		}
	}

}
