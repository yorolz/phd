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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
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

public class GraphTable extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int GRAPH_SIZE_MAXIMUM = 2048;
	private static final int GRAPH_SIZE_MINIMUM = 96;
	private static final String graphDatafile = "graphdata.csv";
	private static final int default_graphSize = 200;
	private static final int default_graphfontSize = 18;

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
					GraphTable frame = new GraphTable();
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
	private int graphfontSize = default_graphfontSize;
	private JPanel panel;
	private JButton btnNewButton;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontScaleLabel;
	private HashMap<String, GraphData> graphMap;

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
	public GraphTable() throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		initialize();
	}

	private void initialize() {
		setTitle("weeeeeee");
		setName("");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		horizontalPanel = new JSplitPane();
		contentPane.add(horizontalPanel, BorderLayout.CENTER);
		horizontalPanel.setEnabled(false);
		horizontalPanel.setContinuousLayout(true);
		horizontalPanel.setResizeWeight(1.0);

		graphPanel = new JPanel();
		scrollPane = new JScrollPane(graphPanel);
		scrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		horizontalPanel.setLeftComponent(scrollPane);
		graphPanel.setBorder(null);
		graphPanel.setLayout(new GridLayout(0, 4, 0, 0));

		settingsPanel = new JPanel();
		settingsPanel.setMinimumSize(new Dimension(256, 10));
		horizontalPanel.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		graphSizePanel = new JPanel();
		graphSizePanel.setBorder(new TitledBorder(null, "Graph Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(graphSizePanel);

		graphSizeSlider = new JSlider(GRAPH_SIZE_MINIMUM, GRAPH_SIZE_MAXIMUM, default_graphSize);
		graphSizeSlider.setPaintLabels(true);
		graphSizePanel.add(graphSizeSlider);
		graphSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				updateGraphsSize(source);
			}
		});

		graphSizeLabel = new JLabel(Integer.toString(graphSizeSlider.getValue()));
		graphSizePanel.add(graphSizeLabel);

		fontScalePanel = new JPanel();
		fontScalePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Font Size", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(fontScalePanel);

		fontScaleSlider = new JSlider(FONT_SIZE_MINIMUM, FONT_SIZE_MAXIMUM, default_graphfontSize);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(default_graphfontSize);
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

		fontScaleLabel = new JLabel(Integer.toString(fontScaleSlider.getValue()));
		fontScalePanel.add(fontScaleLabel);

		panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Misc.", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(panel);

		btnNewButton = new JButton("Show stuff!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel.add(btnNewButton);
	}

	public void initializeTheRest() throws NoSuchFileException, IOException {
		double w = 640 * OSTools.getScreenScale();
		double h = 480 * OSTools.getScreenScale();
		setSize(new Dimension((int) w, (int) h));

		createGraphs();
		// addGraphsToPanel();

		graphAutoLayoutTimeout();

//		pack();
		setLocationRelativeTo(null);
	}

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
		BufferedReader br = new BufferedReader(new FileReader(graphDatafile));
		int counter = 0;
		while (br.ready()) {
			String id = Integer.toString(counter);
			String line = br.readLine();
			GraphData gd = new GraphData(id, line, graphSize);
			DefaultView defaultView = gd.getDefaultView();
			defaultView.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					String clickedComponent = e.getComponent().getName();
					GraphData clickedGD = graphMap.get(clickedComponent);
					clickedGD.toggleSelected();
				}
			});
			graphMap.put(id, gd);
			graphPanel.add(defaultView);
			counter++;
		}
		br.close();
		updateFontsSize();
	}

	protected void setupGraphPanellayout() {
		GridLayout layout = (GridLayout) graphPanel.getLayout();
		int graphsPerColumn = (int) Math.floor(scrollPane.getViewport().getWidth() / graphSize);
		if (layout.getColumns() != graphsPerColumn) {
			if (graphsPerColumn >= 1 && graphsPerColumn <= 8) {
				layout.setColumns(graphsPerColumn);
				layout.setRows(0);
			}
		}
	}

	private void updateGraphsSize(JSlider source) {
		graphSize = source.getValue();
		graphSizeLabel.setText(Integer.toString(source.getValue()));
//		if (source.getValueIsAdjusting())
//			return;
		for (GraphData gd : graphMap.values()) {
			DefaultView view = gd.getDefaultView();
			// both these set*Size and later a validate/revalidate must be called to force the flowlayout to "relayout" its components
			Dimension size = new Dimension(graphSize, graphSize);
			view.setMinimumSize(size);
			view.setMaximumSize(size);
			view.setPreferredSize(size);
			view.setSize(size);
		}
		setupGraphPanellayout();
		graphPanel.revalidate();
	}

	protected void updateFontsSize() {
		for (GraphData gd : graphMap.values()) {
			MultiGraph graph = gd.getMultiGraph();
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphfontSize, graphfontSize);
			graph.addAttribute("ui.stylesheet", style);
		}
	}

	protected void updateGraphFontsSizeControl(JSlider source) {
		graphfontSize = source.getValue();
		fontScaleLabel.setText(Integer.toString(source.getValue()));
		updateFontsSize();
	}

}
