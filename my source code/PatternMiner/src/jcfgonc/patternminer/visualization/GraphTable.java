package jcfgonc.patternminer.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.graphstream.ui.swingViewer.DefaultView;

import graph.GraphReadWrite;
import graph.StringGraph;

public class GraphTable extends JFrame {

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	public static void main(String[] args) throws NoSuchFileException, IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphTable frame = new GraphTable();
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

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	public GraphTable() throws NoSuchFileException, IOException {
		initialize();
		initializeTheRest();
	}

	private void initializeTheRest() throws NoSuchFileException, IOException {
		StringGraph graph = GraphVisualizer.createGraphFromString("X1,partof,X2;X2,atlocation,X0;X2,usedfor,X3;");
		DefaultView graphView = GraphVisualizer.createGraphViewer(graph);
		graphView.setPreferredSize(new Dimension(64, 64));
		graphsPanel.add(graphView);
//		graphsPanel.add(GraphVisualizer.createGraphViewer(GraphVisualizer.createGraphFromString("X2,maininterest,X0;X2,influencedby,X1;X2,notableidea,X3;")));
//		graphsPanel.add(GraphVisualizer.createGraphViewer(GraphVisualizer.createGraphFromString("X0,knownfor,X2;X2,usedfor,X1;X2,hasproperty,X3;")));
//		graphsPanel.add(GraphVisualizer.createGraphViewer(GraphVisualizer.createGraphFromString("X3,antonym,X1;X1,hascontext,X0;X2,partof,X1;")));
	}

	private void initialize() {
		setTitle("weeeeeee");
		setName("");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		graphsPanel = new JPanel();
		graphsPanel.setMinimumSize(new Dimension(608, 432));
		graphsPanel.setBorder(null);
//		scrollPane = new JScrollPane(graphsPanel);
		graphsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
//		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(graphsPanel, BorderLayout.CENTER);

		settingsPanel = new JPanel();
		contentPane.add(settingsPanel, BorderLayout.EAST);

		graphSizeSlider = new JSlider();
		settingsPanel.add(graphSizeSlider);
		graphSizeSlider.setValue(160);
		graphSizeSlider.setMaximum(512);
		graphSizeSlider.setMinimum(32);
		graphSizeSlider.setBorder(new TitledBorder(null, "Graph Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	}

}
