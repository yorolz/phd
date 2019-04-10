package jcfgonc.patternminer.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import slider.RangeSlider;
import utils.OSTools;

public class GraphResultsGUI extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int FONT_SIZE_DEFAULT = 18;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final String graphDatafile = "mergedResults.csv";
	private static final int NODE_SIZE_MINIMUM = 0;
	private static final int NODE_SIZE_DEFAULT = 24;
	private static final int NODE_SIZE_MAXIMUM = 100;
	private static final int NUMBER_VISIBLE_GRAPHS_MINIMUM = 1;
	private static final int NUMBER_VISIBLE_GRAPHS_DEFAULT = 16;
	private static final int NUMBER_VISIBLE_GRAPHS_MAXIMUM = 1024;
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

	private GraphFilter graphFilter;
	private MutableBoolean shiftKeyPressed;
	private HashMap<String, JLabel> minimumColumnLabelMap;
	private HashMap<String, JLabel> maximumColumnLabelMap;
	private int graphFontSize = FONT_SIZE_DEFAULT;
	private int graphNodeSize = NODE_SIZE_DEFAULT;
	private int graphsPerColumn = GRAPHS_PER_COLUMN_DEFAULT;
	private JPanel contentPane;
	private JPanel graphPanel;
	private JScrollPane scrollPane;
	private JPanel settingsPanel;
	private JSlider numColumnsSlider;
	private JSplitPane horizontalPanel;
	private JPanel numColumnsPanel;
	private JLabel numColumnsLabel;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontSizeLabel;
	private JPanel nodeSizePanel;
	private JSlider nodeSizeSlider;
	private JLabel nodeSizeLabel;
	private JPanel renderingControlPanel;
	private JPanel filteringPanel;
	private JCheckBox ascendingCB;
	private JComboBox<String> sortingColumnBox;
	private JCheckBox varRenderCB;
	private JPanel numGraphsPanel;
	private JPanel sortingPanel;
	private JSlider numGraphsSlider;
	private JLabel numGraphsLabel;
	private JPanel graphEditPanel;
	private JButton debugButton;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenuItem selectAllMenuItem;
	private JMenuItem selectNoneMenuItem;
	private JMenuItem invertSelectionMenuItem;
	private JSeparator separator;
	private JMenuItem deleteSelectionMenuItem;
	private JMenuItem cropSelectionMenuItem;
	private JMenuItem exitMenuItem;
	private JMenuItem saveSelectionMenuItem;
	private JMenuItem openFileMenuItem;
	private JMenuItem saveVisibleMenuItem;
	private JLabel sortLabel;
	private JPanel columnsFilterPanel;
	private JMenuItem saveFilteredMenuItem;

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

	@SuppressWarnings("deprecation")
	private void initialize() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\ck\\Desktop\\github\\my source code\\PatternMiner\\icon2.png"));
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

		numGraphsPanel = new JPanel();
		renderingControlPanel.add(numGraphsPanel);
		numGraphsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Graphs per Screen",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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
		renderingControlPanel.add(varRenderCB);

		filteringPanel = new JPanel();
		filteringPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)), "Filtering Options",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(filteringPanel);
		filteringPanel.setLayout(new BoxLayout(filteringPanel, BoxLayout.Y_AXIS));

		sortingPanel = new JPanel();
		filteringPanel.add(sortingPanel);

		sortLabel = new JLabel("Sort");
		sortingPanel.add(sortLabel);

		ascendingCB = new JCheckBox("Ascending");
		sortingPanel.add(ascendingCB);
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
		sortingPanel.add(sortingColumnBox);
		sortingColumnBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				String columnName = (String) cb.getSelectedItem();
				sortGraphs(columnName);
			}
		});

		columnsFilterPanel = new JPanel();
		filteringPanel.add(columnsFilterPanel);
		columnsFilterPanel.setLayout(new BoxLayout(columnsFilterPanel, BoxLayout.Y_AXIS));

		graphEditPanel = new JPanel();
		graphEditPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		filteringPanel.add(graphEditPanel);

		debugButton = new JButton("MEGA button!");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// lol();
				graphFilter.debugButton();
			}
		});
		graphEditPanel.setLayout(new BorderLayout(0, 0));
		graphEditPanel.add(debugButton);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized();
			}
		});

		menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		openFileMenuItem = new JMenuItem("Open File");
		fileMenu.add(openFileMenuItem);

		saveSelectionMenuItem = new JMenuItem("Save Selected Graphs");
		saveSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.saveSelectedGraphs(GraphResultsGUI.this);
			}
		});
		fileMenu.add(saveSelectionMenuItem);

		saveVisibleMenuItem = new JMenuItem("Save Visible Graphs");
		saveVisibleMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.saveVisibleGraphs(GraphResultsGUI.this);
			}
		});
		fileMenu.add(saveVisibleMenuItem);

		saveFilteredMenuItem = new JMenuItem("Save Filtered Graphs");
		saveFilteredMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.saveFilteredGraphs(GraphResultsGUI.this);
			}
		});
		fileMenu.add(saveFilteredMenuItem);

		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		fileMenu.add(exitMenuItem);

		editMenu = new JMenu("Edit");
		menuBar.add(editMenu);

		selectAllMenuItem = new JMenuItem("Select All");
		selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		selectAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});

		deleteSelectionMenuItem = new JMenuItem("Delete Selection");
		deleteSelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedGraphs();
			}
		});
		editMenu.add(deleteSelectionMenuItem);

		cropSelectionMenuItem = new JMenuItem("Crop Selection");
		cropSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cropSelection();
			}
		});
		editMenu.add(cropSelectionMenuItem);

		separator = new JSeparator();
		editMenu.add(separator);
		editMenu.add(selectAllMenuItem);

		selectNoneMenuItem = new JMenuItem("Select None");
		selectNoneMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		selectNoneMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNone();
			}
		});
		editMenu.add(selectNoneMenuItem);

		invertSelectionMenuItem = new JMenuItem("Invert Selection");
		invertSelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		invertSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invertSelection();
			}
		});
		editMenu.add(invertSelectionMenuItem);
	}

	private void quit() {
		System.exit(0);
	}

	@SuppressWarnings("unused")
	private void lol() {
		setState(Frame.ICONIFIED);

		// jframe
		JFrame jf = new JFrame();
		jf.getContentPane().setLayout(new GridLayout());
		JLabel l = new JLabel(new ImageIcon("bsod.png"));
		jf.getContentPane().add(l);
		jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jf.setUndecorated(true);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		device.setFullScreenWindow(jf);

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
		handleKeyEvents();
		setSize(new Dimension((int) w, (int) h));
		graphFilter = new GraphFilter(graphDatafile, NUMBER_VISIBLE_GRAPHS_DEFAULT, shiftKeyPressed);
		addVisibleGraphsToPanel();
//		graphAutoLayoutTimeout();
		setupGraphPanelLayout();
		updateFontsSize();
		updateNodesSize();
		if (!graphFilter.hasVisibleGraphs()) {
			System.err.println("no graph data loaded...");
			System.exit(-1);
		}
		GraphData gd = graphFilter.getVisibleGraphList().get(0);
		Object2IntMap<String> oi = gd.getDetailsHeader();
		String[] columnNames = oi.keySet().toArray(new String[0]);
		// sorting box
		sortingColumnBox.setModel(new DefaultComboBoxModel<String>(columnNames));
		// create the filtering panels
		createFilteringPanels(oi.keySet());
		setLocationRelativeTo(null);
	}

	private void createFilteringPanels(Collection<String> columns) {
		minimumColumnLabelMap = new HashMap<>();
		maximumColumnLabelMap = new HashMap<>();
		for (String column : columns) {
			if (column.startsWith("s:") || column.equals("n:time"))
				continue;

			JPanel columnPanel = new JPanel();
			columnPanel.setBorder(new TitledBorder(null, column.substring(column.indexOf(":") + 1), TitledBorder.LEADING, TitledBorder.TOP, null, null));
			columnsFilterPanel.add(columnPanel);

			double min = graphFilter.getMinimumOfColumn(column);
			double max = graphFilter.getMaximumOfColumn(column);

			JLabel lowLimitLabel = new JLabel(Double.toString(min));
			minimumColumnLabelMap.put(column, lowLimitLabel);
			columnPanel.add(lowLimitLabel);

			RangeSlider rangeSlider = new RangeSlider();
			rangeSlider.setUpperValue(100);
			columnPanel.add(rangeSlider);

			JLabel highLimitLabel = new JLabel(Double.toString(max));
			maximumColumnLabelMap.put(column, highLimitLabel);
			columnPanel.add(highLimitLabel);

			rangeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					RangeSlider slider = (RangeSlider) e.getSource();
					double lowValue = graphFilter.getColumnAdaptedValue(column, slider.getValue());
					double highValue = graphFilter.getColumnAdaptedValue(column, slider.getUpperValue());
					lowLimitLabel.setText(String.format(Locale.ROOT, "%.2f", lowValue));
					highLimitLabel.setText(String.format(Locale.ROOT, "%.2f", highValue));
					if (slider.getValueIsAdjusting())
						return;
				}
			});
			rangeSlider.setValue(0);
		}
		System.out.println();
	}

	private void handleKeyEvents() {
		shiftKeyPressed = new MutableBoolean(false);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						shiftKeyPressed.setTrue();
					}
					break;
				case KeyEvent.KEY_RELEASED:
					if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						shiftKeyPressed.setFalse();
					}
					break;
				}
				return false;
			}
		});
	}

	private void invertSelection() {
		graphFilter.invertSelectionVisible();
	}

	private void selectAll() {
		graphFilter.selectAllVisible();
	}

	private void selectNone() {
		graphFilter.clearSelection();
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
		graphPanel.repaint();
		scrollPane.repaint();
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

	private void cropSelection() {
		graphFilter.cropSelection();
		graphPanel.removeAll();
		addVisibleGraphsToPanel();
		setupGraphPanelLayout();
		updateFontsSize();
		updateNodesSize();
	}

	private void sortGraphs(String columnName) {
		graphFilter.sortGraphs(columnName);
		graphPanel.removeAll();
		addVisibleGraphsToPanel();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void deleteSelectedGraphs() {
		graphFilter.deleteSelection();
		graphPanel.removeAll();
		addVisibleGraphsToPanel();
		setupGraphPanelLayout();
		updateFontsSize();
		updateNodesSize();
	}

}
