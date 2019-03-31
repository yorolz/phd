package jcfgonc.patternminer.visualization;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.CSVReader;

public class GraphData {

	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;
	private List<String> details;
	private List<String> detailsHeader;
	private StringGraph stringGraph;
	private DualHashBidiMap<String, String> conceptVsVar;

	/**
	 * 
	 * @param id            - the ID of this structure
	 * @param graphTriplets - the csv line to be parsed into a graph
	 * @param graphSize     - the size of the graph's screen
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
	public GraphData(String id, StringGraph graph, int graphSize) throws NoSuchFileException, IOException {
		stringGraph = graph;

		multiGraph = GraphGuiCreator.initializeGraphStream();
		GraphGuiCreator.addStringGraphToMultiGraph(multiGraph, stringGraph);

		viewer = new Viewer(multiGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();

		defaultView = (DefaultView) viewer.addDefaultView(false);
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
			StringGraph g = GraphReadWrite.readCSVFromString(row.get(8));
			DualHashBidiMap<String, String> conceptVsVar = createAlternateVertexLabels(g);
			// DualHashBidiMap<String, String> conceptVsVar = new DualHashBidiMap<>(GraphAlgorithms.readMap(row.get(9)));
			GraphData gd = new GraphData(id, g, graphSize);
			gd.setDetailsHeader(header);
			gd.setDetails(row);
			gd.setConceptVsVarBiMap(conceptVsVar);
			graphs.add(gd);
			counter++;
		}
		return graphs;
	}

	private void setConceptVsVarBiMap(DualHashBidiMap<String, String> conceptVsVar) {
		this.conceptVsVar = conceptVsVar;
	}

	private static DualHashBidiMap<String, String> createAlternateVertexLabels(StringGraph g) {
		DualHashBidiMap<String, String> conceptVsVar = new DualHashBidiMap<>();
		Set<String> vertexSet = g.getVertexSet();
		int varCounter = 0;
		for (String concept : vertexSet) {
			String varName = "X" + varCounter;
			conceptVsVar.put(concept, varName);
			varCounter++;
		}
		return conceptVsVar;
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

	public void changeGraphVertexLabelling(boolean alternativeLabelling) {
		Collection<Node> nodeSet = multiGraph.getNodeSet();
		for (Node vertex : nodeSet) {
			String vertexId = vertex.getId();
			String label;
			if (alternativeLabelling) {
				label = conceptVsVar.get(vertexId);
			} else {
				label = vertexId;
			}
			vertex.addAttribute("ui.label", label);
		}
	}

}
