package jcfgonc.patternminer.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.GraphReadWrite;
import graph.StringGraph;
import utils.OSTools;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphTable extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final String graphDatafile = "graphdata.csv";

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
	private JPanel graphsPanel;
	private JScrollPane scrollPane;
	private JPanel settingsPanel;
	private JSlider graphSizeSlider;
	private JSplitPane horizontalPanel;
	private ArrayList<DefaultView> graphViewList;
	private JPanel graphSizePanel;
	private JLabel graphSizeLabel;
	private int graphSize = 200;
	private double graphfontScale = 100;
	private JPanel panel;
	private JButton btnNewButton;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontScaleLabel;
	private ArrayList<MultiGraph> graphList;

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
		setPreferredSize(new Dimension(640, 480));
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

		graphsPanel = new JPanel();
		scrollPane = new JScrollPane(graphsPanel);
		scrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		horizontalPanel.setLeftComponent(scrollPane);
		graphsPanel.setBorder(null);
		graphsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

		settingsPanel = new JPanel();
		settingsPanel.setMinimumSize(new Dimension(256, 10));
		horizontalPanel.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		graphSizePanel = new JPanel();
		graphSizePanel.setBorder(new TitledBorder(null, "Graph Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(graphSizePanel);

		graphSizeSlider = new JSlider(64, 1024, graphSize);
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

		fontScaleSlider = new JSlider(20, 200, (int) graphfontScale);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(100);
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
				showStuff(e);
			}
		});
		panel.add(btnNewButton);
	}

	protected void showStuff(ActionEvent e) {
		double graphsPerView = (double) scrollPane.getWidth() / graphSize;
		System.out.println(scrollPane.getWidth() + "\t" + graphSize + "\t" + graphsPerView + "\t" + graphsPanel.getWidth());
	}

	public void initializeTheRest() throws NoSuchFileException, IOException {
		double w = getPreferredSize().getWidth() * OSTools.getScreenScale();
		double h = getPreferredSize().getHeight() * OSTools.getScreenScale();
		setSize(new Dimension((int) w, (int) h));

		loadGraphData();

		pack();
	}

	private void loadGraphData() throws FileNotFoundException, IOException, NoSuchFileException {
		graphViewList = new ArrayList<DefaultView>();
		graphList = new ArrayList<MultiGraph>();
		BufferedReader br = new BufferedReader(new FileReader(graphDatafile));
		while (br.ready()) {
			String line = br.readLine();
			MultiGraph graph = GraphGuiCreator.createGraph(GraphReadWrite.readCSVFromString(line));

			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
			viewer.enableAutoLayout();
			DefaultView view = (DefaultView) viewer.addDefaultView(false);

			Dimension size = new Dimension(graphSize, graphSize);
			view.setMinimumSize(size);
			view.setMaximumSize(size);
			view.setPreferredSize(size);
			view.setBorder(new LineBorder(Color.BLACK));
			graphsPanel.add(view);
			graphViewList.add(view);
			graphList.add(graph);
		}
		br.close();
		updateGraphFontsSize();
	}

	private void updateGraphsSize(JSlider source) {
		graphSize = source.getValue();
		graphSizeLabel.setText(Integer.toString(source.getValue()));
//		if (source.getValueIsAdjusting())
//			return;
		updateGraphFontsSize();
		for (DefaultView graph : graphViewList) {
			// both these set*Size and later a validate/revalidate must be called to force the flowlayout to "relayout" its components
			Dimension size = new Dimension(graphSize, graphSize);
			graph.setMinimumSize(size);
			graph.setMaximumSize(size);
			graph.setPreferredSize(size);
			graph.setSize(size);
		}
		graphsPanel.revalidate();
	}

	protected void updateGraphFontsSize() {
		int fontSize = (int) (graphSize * graphfontScale / 1000.0);
		if (fontSize < 1) {
			fontSize = 1;
		}
		for (MultiGraph graph : graphList) {
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", fontSize, fontSize);
			graph.addAttribute("ui.stylesheet", style);
		}
	}

	protected void updateGraphFontsSizeControl(JSlider source) {
		graphfontScale = source.getValue();
		fontScaleLabel.setText(Integer.toString(source.getValue()));
		updateGraphFontsSize();
	}

}
