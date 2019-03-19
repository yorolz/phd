package jcfgonc.patternminer.visualization;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.GraphReadWrite;
import graph.StringGraph;

public class GraphData {

	private StringGraph stringGraph;
	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;

	/**
	 * 
	 * @param id        - the ID of this structure
	 * @param csvLine   - the csv line to be parsed into a graph
	 * @param graphSize - the size of the graph's screen
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
	public GraphData(String id, String csvLine, int graphSize) throws NoSuchFileException, IOException {
		this.stringGraph = GraphReadWrite.readCSVFromString(csvLine);
		this.multiGraph = GraphGuiCreator.createGraph(stringGraph);

		this.viewer = new Viewer(multiGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();

		this.defaultView = (DefaultView) viewer.addDefaultView(false);
		Dimension size = new Dimension(graphSize, graphSize);
		defaultView.setMinimumSize(size);
		defaultView.setMaximumSize(size);
		defaultView.setPreferredSize(size);
		// view.setBorder(new LineBorder(Color.BLACK));
		defaultView.setName(id);
		setSelected(false);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (isSelected()) {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,128); }");
		} else {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,0); }");
		}
	}

	public StringGraph getStringGraph() {
		return stringGraph;
	}

	public MultiGraph getMultiGraph() {
		return multiGraph;
	}

	public Viewer getViewer() {
		return viewer;
	}

	public DefaultView getDefaultView() {
		return defaultView;
	}

	public void toggleSelected() {
		setSelected(!isSelected());
	}

}
