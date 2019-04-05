package jcfgonc.patternminer.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
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
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import utils.OSTools;

public class GraphResultsGUI extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int FONT_SIZE_DEFAULT = 18;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final String graphDatafile = "mergedResultsBig.csv";
	private static final int NODE_SIZE_MINIMUM = 0;
	private static final int NODE_SIZE_DEFAULT = 24;
	private static final int NODE_SIZE_MAXIMUM = 100;
	private static final int NUMBER_VISIBLE_GRAPHS_MINIMUM = 1;
	private static final int NUMBER_VISIBLE_GRAPHS_DEFAULT = 32;
	private static final int NUMBER_VISIBLE_GRAPHS_MAXIMUM = 256;
	private static final int GRAPHS_PER_COLUMN_MINIMUM = 1;
	private static final int GRAPHS_PER_COLUMN_DEFAULT = 4;
	private static final int GRAPHS_PER_COLUMN_MAXIMUM = 10;

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
	private JSlider numColumnsSlider;
	private JSplitPane horizontalPanel;
	private JPanel numColumnsPanel;
	private JLabel numColumnsLabel;
	private int graphFontSize = FONT_SIZE_DEFAULT;
	private int graphNodeSize = NODE_SIZE_DEFAULT;
	private int graphsPerColumn = GRAPHS_PER_COLUMN_DEFAULT;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontSizeLabel;
	private JPanel nodeSizePanel;
	private JSlider nodeSizeSlider;
	private JLabel nodeSizeLabel;
	private JPanel renderingControlPanel;
	private JPanel searchPanel;
	private JCheckBox ascendingCB;
	private JComboBox<String> sortingColumnBox;
	private JCheckBox varRenderCB;
	private JPanel numGraphsPanel;
	private JPanel sortPanel;
	private JSlider numGraphsSlider;
	private JLabel numGraphsLabel;
	private GraphFilter graphFilter;
	private JPanel graphEditPanel;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	private JButton btnNewButton_2;
	private JButton btnNewButton_3;
	private JButton btnNewButton_4;
	private JButton btnClearSelection;

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
		graphPanel.setLayout(new GridLayout(1, 0, 0, 0));
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

		numColumnsPanel = new JPanel();
		renderingControlPanel.add(numColumnsPanel);
		numColumnsPanel.setBorder(new TitledBorder(null, "Graphs per Column", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		numColumnsSlider = new JSlider(GRAPHS_PER_COLUMN_MINIMUM, GRAPHS_PER_COLUMN_MAXIMUM, GRAPHS_PER_COLUMN_DEFAULT);
		numColumnsSlider.setPaintLabels(true);
		numColumnsPanel.add(numColumnsSlider);
		numColumnsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateGraphsColumnControl(source);
			}
		});

		numColumnsLabel = new JLabel(Integer.toString(4));
		numColumnsPanel.add(numColumnsLabel);

		fontScalePanel = new JPanel();
		renderingControlPanel.add(fontScalePanel);
		fontScalePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Font Size", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));

		fontScaleSlider = new JSlider(FONT_SIZE_MINIMUM, FONT_SIZE_MAXIMUM, FONT_SIZE_DEFAULT);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(FONT_SIZE_DEFAULT);
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

		nodeSizeSlider = new JSlider(NODE_SIZE_MINIMUM, NODE_SIZE_MAXIMUM, NODE_SIZE_DEFAULT);
		nodeSizeSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					nodeSizeSlider.setValue(NODE_SIZE_DEFAULT);
				}
			}
		});
		nodeSizeSlider.setPaintLabels(true);
		nodeSizePanel.add(nodeSizeSlider);
		nodeSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateNodeSizeControl(source);
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
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

		sortPanel = new JPanel();
		searchPanel.add(sortPanel);

		ascendingCB = new JCheckBox("Ascending");
		sortPanel.add(ascendingCB);
		ascendingCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				graphFilter.setSortAscending(e.getStateChange() == ItemEvent.SELECTED);
				String columnName = (String) sortingColumnBox.getSelectedItem();
				sortGraphs(columnName);
			}
		});
		ascendingCB.setHorizontalAlignment(SwingConstants.CENTER);

		sortingColumnBox = new JComboBox<>();
		sortPanel.add(sortingColumnBox);
		sortingColumnBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				String columnName = (String) cb.getSelectedItem();
				sortGraphs(columnName);
			}
		});

		numGraphsPanel = new JPanel();
		numGraphsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Number of Graphs to Show",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		searchPanel.add(numGraphsPanel);
		numGraphsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		numGraphsSlider = new JSlider(NUMBER_VISIBLE_GRAPHS_MINIMUM, NUMBER_VISIBLE_GRAPHS_MAXIMUM, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		numGraphsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateNumberVisibleGraphsControl(source);
			}
		});
		numGraphsPanel.add(numGraphsSlider);

		numGraphsLabel = new JLabel(Integer.toString(NUMBER_VISIBLE_GRAPHS_DEFAULT));
		numGraphsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		numGraphsPanel.add(numGraphsLabel);

		graphEditPanel = new JPanel();
		searchPanel.add(graphEditPanel);
		graphEditPanel.setLayout(new GridLayout(0, 2, 0, 0));

		btnNewButton = new JButton("Delete Selected");
		graphEditPanel.add(btnNewButton);

		btnNewButton_2 = new JButton("Save Selected");
		graphEditPanel.add(btnNewButton_2);

		btnNewButton_3 = new JButton("Crop Selected");
		graphEditPanel.add(btnNewButton_3);

		btnNewButton_4 = new JButton("Save Visible");
		graphEditPanel.add(btnNewButton_4);

		btnNewButton_1 = new JButton("Restore Deleted");
		graphEditPanel.add(btnNewButton_1);

		btnClearSelection = new JButton("Clear Selection");
		btnClearSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSelection();
			}
		});
		graphEditPanel.add(btnClearSelection);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized();
			}
		});

	}

	protected void clearSelection() {
		graphFilter.clearSelection();
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
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			gd.changeGraphVertexLabelling(alternativeLabelling);
		}
	}

	private void updateGraphVisibility(ChangeEvent e) {
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			// gd.getViewer().disableAutoLayout();
			DefaultView defaultView = gd.getDefaultView();
			boolean isVisible = !defaultView.getVisibleRect().isEmpty();
			defaultView.setEnabled(isVisible);
			defaultView.setVisible(isVisible);
		}
	}

	private void windowResized() {
		setupGraphPanelLayout();
	}

	public void initializeTheRest() throws NoSuchFileException, IOException {
		double w = 640 * OSTools.getScreenScale();
		double h = 480 * OSTools.getScreenScale();
		setSize(new Dimension((int) w, (int) h));
		graphFilter = new GraphFilter(graphDatafile, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		addVisibleGraphsToPanel();
//		graphAutoLayoutTimeout();
		setupGraphPanelLayout();
		updateFontsSize();
		updateNodesSize();
		if (graphFilter.hasVisibleGraphs()) {
			GraphData gd = graphFilter.getVisibleGraph(0);
			Object2IntMap<String> oi = gd.getDetailsHeader();
			String[] columnNames = oi.keySet().toArray(new String[0]);
			sortingColumnBox.setModel(new DefaultComboBoxModel<String>(columnNames));
		}
		setLocationRelativeTo(null);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "terminate");
		rootPane.getActionMap().put("terminate", new AbstractAction() {
			private static final long serialVersionUID = 9166263852487334840L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}

	@SuppressWarnings("unused")
	private void graphAutoLayoutTimeout() {
		Thread timeOutThread = new Thread() {
			public void run() {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
				for (GraphData gd : graphFilter.getVisibleGraphList()) {
					gd.getViewer().disableAutoLayout();
				}
			}
		};
		timeOutThread.start();
	}

	private void addVisibleGraphsToPanel() {
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			graphPanel.add(gd.getDefaultView());
		}
	}

	private void setupGraphPanelLayout() {
		int nVisibleG = graphFilter.getVisibleGraphList().size();
		if (nVisibleG < graphsPerColumn) {
			graphsPerColumn = nVisibleG;
		}
		int panelWidth = scrollPane.getViewport().getWidth();
		int graphSize = panelWidth / graphsPerColumn - 2;
//		scrollPane.getVerticalScrollBar().setUnitIncrement(4);
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			DefaultView view = gd.getDefaultView();
			Dimension size = new Dimension(graphSize, graphSize);
			view.setPreferredSize(size);
		}
		GridLayout layout = (GridLayout) graphPanel.getLayout();
		layout.setColumns(graphsPerColumn);
		layout.setRows(0);
		graphPanel.revalidate();
		numColumnsLabel.setText(Integer.toString(graphsPerColumn));
		// numGraphsSlider.setValue(graphsPerColumn);
	}

	private void updateFontsSize() {
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			MultiGraph graph = gd.getMultiGraph();
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			graph.addAttribute("ui.stylesheet", style);
		}
	}

	private void updateGraphsColumnControl(JSlider source) {
		graphsPerColumn = source.getValue();
		setupGraphPanelLayout();
	}

	private void updateNumberVisibleGraphsControl(JSlider source) {
		numGraphsLabel.setText(Integer.toString(source.getValue()));
		if (source.getValueIsAdjusting())
			return;
		graphFilter.setNumberVisibleGraphs(source.getValue());
		// --
		graphPanel.removeAll();
		addVisibleGraphsToPanel();
		setupGraphPanelLayout();
		updateFontsSize();
		updateNodesSize();
		graphPanel.validate();
		graphPanel.repaint();
	}

	private void updateGraphFontsSizeControl(JSlider source) {
		graphFontSize = source.getValue();
		fontSizeLabel.setText(Integer.toString(graphFontSize));
		updateFontsSize();
	}

	private void updateNodeSizeControl(JSlider source) {
		graphNodeSize = source.getValue();
		nodeSizeLabel.setText(Integer.toString(graphNodeSize));
		updateNodesSize();
	}

	private void updateNodesSize() {
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
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

	private void sortGraphs(String columnName) {
		graphFilter.sortGraphs(columnName);
		graphPanel.removeAll();
		addVisibleGraphsToPanel();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

}
