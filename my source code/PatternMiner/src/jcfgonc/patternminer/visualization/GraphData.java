package jcfgonc.patternminer.visualization;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.CSVReader;

public class GraphData {

	private StringGraph stringGraph;
	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;
	private List<String> details;
	private List<String> detailsHeader;

	/**
	 * 
	 * @param id            - the ID of this structure
	 * @param graphTriplets - the csv line to be parsed into a graph
	 * @param graphSize     - the size of the graph's screen
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
	public GraphData(String id, StringGraph stringGraph, int graphSize) throws NoSuchFileException, IOException {
		this.stringGraph = stringGraph;// GraphReadWrite.readCSVFromString(graphTriplets);
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

	public String getId() {
		return defaultView.getName();
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

	public static ArrayList<GraphData> createGraphsFromCSV(String columnSeparator, File file, boolean fileHasHeader, int graphSize) throws IOException {
		ArrayList<GraphData> graphs = new ArrayList<>();
		List<List<String>> data = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		int counter = 0;
		Iterator<List<String>> rowIt = data.iterator();
		List<String> header = rowIt.next();
		while (rowIt.hasNext()) {
			List<String> row = rowIt.next();
			String id = Integer.toString(counter);
			String graphStr = row.get(8);
			StringGraph sg = GraphReadWrite.readCSVFromString(graphStr);
			GraphData gd = new GraphData(id, sg, graphSize);
			gd.setDetailsHeader(header);
			gd.setDetails(row);
			graphs.add(gd);
			counter++;
		}
		return graphs;
	}

	private void setDetails(List<String> row) {
		this.details = row;
	}

	private void setDetailsHeader(List<String> header) {
		this.detailsHeader = header;
	}

	public List<String> getDetails() {
		return details;
	}

	public List<String> getDetailsHeader() {
		return detailsHeader;
	}

}
