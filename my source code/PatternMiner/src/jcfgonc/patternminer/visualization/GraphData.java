package jcfgonc.patternminer.visualization;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
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
import org.graphstream.ui.view.util.DefaultMouseManager;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.CSVReader;

public class GraphData {

	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;
	private Int2ObjectMap<String> detailsMap;
	private Object2IntMap<String> detailsHeader;
	private StringGraph stringGraph;
	private DualHashBidiMap<String, String> conceptVsVar;
	private boolean loaded;
	private String id;
	private boolean layoutStarted;
	private boolean layoutTerminated;
	private MouseAdapter mouseAdapter;

	/**
	 * 
	 * @param id            - the ID of this structure
	 * @param int2ObjectMap
	 * @param header
	 * @param graphTriplets - the csv line to be parsed into a graph
	 * @param graphSize     - the size of the graph's screen
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
	public GraphData(String id, StringGraph graph, Object2IntMap<String> detailsHeader, Int2ObjectMap<String> detailsMap) throws NoSuchFileException, IOException {
		this.id = id;
		this.stringGraph = graph;
		this.detailsMap = detailsMap;
		this.detailsHeader = detailsHeader;
		this.conceptVsVar = createAlternateVertexLabels(graph);
		this.loaded = false;
		this.layoutStarted = false;
		this.layoutTerminated = false;
		this.selected = false;
		this.multiGraph = null;
		this.viewer = null;
		this.defaultView = null;
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return this.id;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (!loaded)
			return;
		if (isSelected()) {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,128); }");
		} else {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,0); }");
		}
	}
	
	public MultiGraph getMultiGraph() {
		lazyLoad();
		return multiGraph;
	}

	public Viewer getViewer() {
		lazyLoad();
		return viewer;
	}

	public DefaultView getDefaultView() {
		lazyLoad();
		return defaultView;
	}

	private void lazyLoad() {
		if (!loaded) {
			this.multiGraph = GraphGuiCreator.initializeGraphStream();
			GraphGuiCreator.addStringGraphToMultiGraph(multiGraph, stringGraph);
			viewer = new Viewer(multiGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
			defaultView = (DefaultView) viewer.addDefaultView(false);
			// defaultView.setBorder(new LineBorder(Color.BLACK));
			defaultView.setName(id);
			defaultView.setToolTipText(createToolTipText());
			defaultView.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					super.componentShown(e);
					if (!layoutStarted) {
						try {
							viewer.enableAutoLayout();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						layoutStarted = true;
					}
				}

				@Override
				public void componentHidden(ComponentEvent e) {
					super.componentHidden(e);
					if (layoutTerminated) {
						return;
					}
					layoutTerminated = true;
					new Thread() {
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
							}
							viewer.disableAutoLayout();
						};
					}.start();
				}
			});
			DefaultMouseManager manager = new DefaultMouseManager();
			defaultView.setMouseManager(manager);
			manager.release();

			defaultView.addMouseListener(mouseAdapter);
			loaded = true;
		}
	}

	public void toggleSelected() {
		lazyLoad();
		setSelected(!isSelected());
	}

	public static ArrayList<GraphData> createGraphsFromCSV(String columnSeparator, File file, boolean fileHasHeader) throws IOException {
		ArrayList<GraphData> graphs = new ArrayList<>();
		List<List<String>> data = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		int counter = 0;
		Iterator<List<String>> rowIt = data.iterator();
		Object2IntMap<String> header = headerToMap(rowIt.next());
		while (rowIt.hasNext()) {
			List<String> row = rowIt.next();
			String id = Integer.toString(counter);
			StringGraph g = GraphReadWrite.readCSVFromString(row.get(8));
			// DualHashBidiMap<String, String> conceptVsVar = new DualHashBidiMap<>(GraphAlgorithms.readMap(row.get(9)));
			GraphData gd = new GraphData(id, g, header, rowToMap(row));
			graphs.add(gd);
			counter++;
		}
		return graphs;
	}

	private static Int2ObjectMap<String> rowToMap(List<String> row) {
		Int2ObjectArrayMap<String> map = new Int2ObjectArrayMap<>();
		for (int i = 0; i < row.size(); i++) {
			map.put(i, row.get(i));
		}
		return map;
	}

	private static Object2IntMap<String> headerToMap(List<String> row) {
		Object2IntMap<String> map = new Object2IntOpenHashMap<>();
		for (int i = 0; i < row.size(); i++) {
			map.put(row.get(i), i);
		}
		return map;
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

	private String createToolTipText() {
		// n:time n:relationTypes n:relationTypesStd n:cycles n:patternEdges n:patternVertices n:matches s:query s:pattern s:conceptVarMap s:hash
		String text = "<html>";
		for (String column : detailsHeader.keySet()) {
			if (column.equals("s:query") || column.equals("s:pattern") || column.equals("s:conceptVarMap") || column.equals("s:hash"))
				continue;
			int columnId = detailsHeader.getInt(column);
			String value = detailsMap.get(columnId);
			text += String.format("%s:\t%s<br>", column, value);
		}
		return text;
	}

	public Int2ObjectMap<String> getDetails() {
		return detailsMap;
	}

	public String getDetails(String column) {
		int columnId = detailsHeader.getInt(column);
		String value = detailsMap.get(columnId);
		return value;
	}

	public Object2IntMap<String> getDetailsHeader() {
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

	public void addMouseListener(MouseAdapter mouseAdapter) {
		this.mouseAdapter = mouseAdapter;
	}

}
