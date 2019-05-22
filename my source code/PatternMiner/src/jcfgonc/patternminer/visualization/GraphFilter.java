package jcfgonc.patternminer.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.mutable.MutableBoolean;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import structures.GlobalFileWriter;
import structures.TypeMap;

public class GraphFilter {
	private HashMap<String, GraphData> graphMap;
	private ArrayList<GraphData> visibleGraphList;
	/**
	 * original unmodified graph list
	 */
	private ArrayList<GraphData> originalGraphList;
	/**
	 * copy of the graph list which is progressively edited (has graphs removed)
	 */
	private ArrayList<GraphData> graphList;
	private int numberVisibleGraphs = 0;
	private boolean sortAscending;
	private GraphData currentlyClickedGD;
	private GraphData lastClickedGD;
	/**
	 * graphs which are currently selected (and highlighted)
	 */
	private HashSet<GraphData> selectedGraphs;
	/**
	 * graphs which the user marked as deleted
	 */
	private HashSet<GraphData> deletedGraphs;
	/**
	 * graphs which are currently being selected with shift+mouse click
	 */
	private HashSet<GraphData> shiftSelectedGraphs;
	/**
	 * global status of the shift key
	 */
	private MutableBoolean shiftKeyPressed;
	private Object2DoubleOpenHashMap<String> minimumOfColumn;
	private Object2DoubleOpenHashMap<String> maximumOfColumn;
	private Object2DoubleOpenHashMap<String> lowHighColumnDifference;
	private Object2DoubleOpenHashMap<String> columnFilterLow;
	private Object2DoubleOpenHashMap<String> columnFilterHigh;
	private TypeMap columnsTypes;
	private HashMap<String, String> columnKey2Description;
	private HashMap<String, String> columnDescription2Key;

	public GraphFilter(String graphDatafile, int numberShownGraphs, MutableBoolean shiftKeyPressed) throws IOException {
		this.graphMap = new HashMap<>();
		this.shiftKeyPressed = shiftKeyPressed;
		this.visibleGraphList = new ArrayList<>();
		this.selectedGraphs = new HashSet<>();
		this.deletedGraphs = new HashSet<GraphData>();
		this.shiftSelectedGraphs = new HashSet<>();
		this.minimumOfColumn = new Object2DoubleOpenHashMap<>();
		this.maximumOfColumn = new Object2DoubleOpenHashMap<>();
		this.lowHighColumnDifference = new Object2DoubleOpenHashMap<>();
		this.columnFilterLow = new Object2DoubleOpenHashMap<>();
		this.columnFilterHigh = new Object2DoubleOpenHashMap<>();
		this.columnsTypes = new TypeMap();
		columnsTypes.loadFromTextFile(new File("config" + File.separator + "columnsTypes.txt"));
		this.columnKey2Description = new HashMap<>();
		this.columnDescription2Key = new HashMap<>();
		loadColumnsDescriptions(new File("config" + File.separator + "columnsDescriptions.txt"));

		System.out.println("loading " + graphDatafile);
		this.originalGraphList = GraphData.createGraphsFromCSV("\t", new File(graphDatafile), true, columnKey2Description);
		System.out.format("%d graphs loaded\n", originalGraphList.size());

		System.out.format("adding MouseClickHandler\n");
		this.graphList = new ArrayList<>(originalGraphList);

		if (graphList.isEmpty())
			return;

		for (GraphData gd : originalGraphList) {
			addMouseClickHandler(gd);
			graphMap.put(gd.getId(), gd);
		}

		System.out.format("setNumberVisibleGraphs()\n");
		setNumberVisibleGraphs(numberShownGraphs);

		System.out.format("GraphFilter() done\n");

	}

	/**
	 * Internally type is stored as an int value reflecting the constants defined in TypeMap
	 * 
	 * @return
	 */
	public TypeMap getColumnTypeMap() {
		return columnsTypes;
	}

	public String getColumnIdFromDescription(String description) {
		return columnDescription2Key.get(description);
	}

	public String getColumnDescription(String id) {
		return columnKey2Description.get(id);
	}

	private void loadColumnsDescriptions(File filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split("[\t]+", 2);
			if (tokens.length != 2)
				continue;
			String id = tokens[0];
			String description = tokens[1];
			columnKey2Description.put(id, description);
			columnDescription2Key.put(description, id);
		}
		reader.close();
	}

	private void addMouseClickHandler(GraphData gd) {
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					String clickedComponent = e.getComponent().getName();
					if (shiftKeyPressed.isTrue()) {
						GraphData currentClickedTemp = getGraph(clickedComponent);
						int i0 = visibleGraphList.indexOf(currentClickedTemp);
						int i1;
						if (currentlyClickedGD == null) {
							i1 = 0;
						} else {
							i1 = visibleGraphList.indexOf(currentlyClickedGD);
						}
						unselectGraphs(shiftSelectedGraphs);
						shiftSelectedGraphs.clear();
						for (int i = Math.min(i0, i1); i <= Math.max(i0, i1); i++) {
							GraphData g = visibleGraphList.get(i);
							g.setSelected(true);
							selectedGraphs.add(g);
							shiftSelectedGraphs.add(g);
						}
					} else {
						shiftSelectedGraphs.clear();
						lastClickedGD = currentlyClickedGD;
						currentlyClickedGD = getGraph(clickedComponent);
						toggleSelected(currentlyClickedGD);
						setGraphBorderState(lastClickedGD, false);
						setGraphBorderState(currentlyClickedGD, true);
					}
				}
			}

			private void unselectGraphs(Collection<GraphData> graphs) {
				if (graphs == null || graphs.isEmpty())
					return;
				graphs.parallelStream().forEach(gd -> {
					gd.setSelected(false);
				});
				selectedGraphs.removeAll(graphs);
			}
		};
		gd.setMouseListener(mouseAdapter);
	}

	private void setGraphBorderState(GraphData gd, boolean enabled) {
		if (gd == null)
			return;
		if (enabled) {
			gd.getDefaultView().setBorder(new LineBorder(Color.BLACK));
		} else {
			gd.getDefaultView().setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}

	/**
	 * returns the loaded graph with the given id
	 * 
	 * @param id
	 * @return
	 */
	public GraphData getGraph(String id) {
		return graphMap.get(id);
	}

	/**
	 * returns the number of loaded graphs
	 * 
	 * @return
	 */
	public int getNumberOfGraphs() {
		return graphList.size();
	}

	/**
	 * returns the number of graphs in the visible list
	 * 
	 * @return
	 */
	public int getNumberOfVisibleGraphs() {
		return visibleGraphList.size();
	}

	/**
	 * returns the visible graph list
	 * 
	 * @return
	 */
	public ArrayList<GraphData> getVisibleGraphList() {
		return visibleGraphList;
	}

	/**
	 * true if there are graphs in the visible list, false otherwise
	 * 
	 * @return
	 */
	public boolean hasVisibleGraphs() {
		return !visibleGraphList.isEmpty();
	}

	public void setSortAscending(boolean b) {
		this.sortAscending = b;
	}

	/**
	 * sets the number of graphs in the visible list, deleting or copying from the loaded list as needed
	 * 
	 * @param num
	 */
	public void setNumberVisibleGraphs(int num) {
		if (num > originalGraphList.size()) {
			num = originalGraphList.size();
		}

		if (num > numberVisibleGraphs) {// fill
			numberVisibleGraphs = num;
			fillWithGraphs();
		} else if (num < numberVisibleGraphs) {// crop
			numberVisibleGraphs = num;
			visibleGraphList = new ArrayList<GraphData>(visibleGraphList.subList(0, numberVisibleGraphs));
		}
	}

	private void updateVisibleList() {
		visibleGraphList.clear();
		fillWithGraphs();
	}

	private void fillWithGraphs() {
		HashSet<GraphData> _visibleGraphSet = new HashSet<>(visibleGraphList);
		Iterator<GraphData> graphListIterator = graphList.iterator();
		while (visibleGraphList.size() < numberVisibleGraphs && graphListIterator.hasNext()) {
			GraphData gd = graphListIterator.next();

			if (deletedGraphs.contains(gd))
				continue;
			if (_visibleGraphSet.contains(gd))
				continue;
			visibleGraphList.add(gd);
			_visibleGraphSet.add(gd);
		}
	}

	public void operatorFilterGraphs() {
		if (columnFilterLow.isEmpty() && columnFilterHigh.isEmpty())
			return;
		graphList.clear();
		outer: for (GraphData gd : originalGraphList) {
			if (deletedGraphs.contains(gd))
				continue;
			for (String column : columnFilterLow.keySet()) {
				double val = Double.parseDouble(gd.getDetails(column));
				final double tol = 0.001;
				double low = columnFilterLow.getDouble(column);
				double high = columnFilterHigh.getDouble(column);
				boolean check = val >= low - tol && val <= high + tol;
				if (!check)
					continue outer;
			}
			graphList.add(gd);
		}
		updateVisibleList();
		operatorClearSelection();
		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	public void setGraphFilter(String column, double lowValue, double highValue) {
		columnFilterLow.put(column, lowValue);
		columnFilterHigh.put(column, highValue);
	}

	public void operatorSortGraphs(String columnName) {
		// sort the full list and then copy to the visible list
		graphList.sort(new Comparator<GraphData>() {

			@Override
			public int compare(GraphData o1, GraphData o2) {
				String d1 = o1.getDetails(columnName);
				String d2 = o2.getDetails(columnName);
				int comp = 0;
				if (columnsTypes.isTypeNumeric(columnName)) {
					double v1 = Double.parseDouble(d1);
					double v2 = Double.parseDouble(d2);
					comp = Double.compare(v1, v2);
				} else if (columnsTypes.isTypeString(columnName)) {
					comp = d1.compareTo(d2);
				} else {
					System.err.println("unknown type for column " + columnName);
					System.exit(-2);
				}
				if (sortAscending) {
					return comp;
				} else {
					return -comp;
				}
			}
		});
		updateVisibleList();
		operatorClearSelection();
		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	public void operatorClearSelection() {
		ArrayList<GraphData> graphs = new ArrayList<>();
		graphs.addAll(selectedGraphs);
		if (lastClickedGD != null) {
			graphs.add(currentlyClickedGD);
		}
		if (currentlyClickedGD != null) {
			graphs.add(currentlyClickedGD);
		}
		selectedGraphs.parallelStream().forEach(graph -> {
			graph.setSelected(false);
			setGraphBorderState(graph, false);
		});
		selectedGraphs.clear();
	}

	public void operatorRestoreDeletedGraphs() {
		deletedGraphs.clear();
		graphList = new ArrayList<>(originalGraphList);
		visibleGraphList.clear();
		fillWithGraphs(); // to force re-ordering from copy
		// mark previously selected graphs as not selected
	}

	private void deleteAndFill(Collection<GraphData> toDelete) {
		// only allow visible graphs to be removed (prevent removal of stuff not in the visible window)
		ArrayList<GraphData> _toDelete = new ArrayList<>(16);
		HashSet<GraphData> visibleSet = new HashSet<>(visibleGraphList);
		for (GraphData gd : toDelete) {
			if (visibleSet.contains(gd)) {
				_toDelete.add(gd);
			}
		}

		visibleGraphList.removeAll(_toDelete);
		graphList.removeAll(_toDelete);
		deletedGraphs.addAll(_toDelete);
		// fill with new graphs properly ordered
		fillWithGraphs();
	}

	public void operatorDeleteSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// remove selection
		deleteAndFill(selectedGraphs);
	}

	public void operatorCropSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// delete all visible graphs which are not selected
		ArrayList<GraphData> toDelete = new ArrayList<>();
		for (GraphData gd : visibleGraphList) {
			if (!selectedGraphs.contains(gd)) {
				toDelete.add(gd);
			}
		}
		deleteAndFill(toDelete);
	}

	public void operatorSelectAllVisible() {
		selectedGraphs.clear();
		selectedGraphs.addAll(visibleGraphList);
		visibleGraphList.parallelStream().forEach(graph -> {
			graph.setSelected(true);
		});
	}

	public void operatorInvertSelectionVisible() {
		visibleGraphList.parallelStream().forEach(graph -> {
			toggleSelected(graph);
		});
	}

	private void toggleSelected(GraphData gd) {
		gd.toggleSelected();
		if (gd.isSelected()) {
			selectedGraphs.add(gd);
		} else {
			selectedGraphs.remove(gd);
		}
	}

	public void saveSelectedGraphs(Component parentComponent) {
		saveGraphsSingleCSV(selectedGraphs, "selected", "Saving selected graphs", parentComponent);
	}

	public void saveFilteredGraphs(Component parentComponent) {
		saveGraphsSingleCSV(graphList, "filtered", "Saving filtered graphs", parentComponent);
	}

	private void saveGraphsSingleCSV(Collection<GraphData> graphs, String suggestion, String title, Component parentComponent) {
		if (graphs.isEmpty()) {
			JOptionPane.showMessageDialog(parentComponent, "Nothing to save");
			return;
		}

		GlobalFileWriter.setExtension(".csv");
		String filename = (String) JOptionPane.showInputDialog(parentComponent, "Type the filename", title, JOptionPane.PLAIN_MESSAGE, null, null, suggestion);
		if (filename == null || filename.trim().isEmpty()) {
			GlobalFileWriter.createNewFile();
		} else {
			GlobalFileWriter.createNewFile(filename);
		}

		GlobalFileWriter
				.writeLine("n:time\tn:relationTypes\tn:relationTypesStd\tn:cycles\tn:patternEdges\tn:patternVertices\tn:matches\ts:query\ts:pattern\ts:conceptVarMap\ts:hash");
		for (GraphData gd : graphs) {
			String line = gd.getDetails("n:time") + //
					"\t" + gd.getDetails("n:relationTypes") + //
					"\t" + gd.getDetails("n:relationTypesStd") + //
					"\t" + gd.getDetails("n:cycles") + //
					"\t" + gd.getDetails("n:patternEdges") + //
					"\t" + gd.getDetails("n:patternVertices") + //
					"\t" + gd.getDetails("n:matches") + //
					"\t" + gd.getDetails("s:query") + //
					"\t" + gd.getDetails("s:pattern") + //
					"\t" + gd.getDetails("s:conceptVarMap") + //
					"\t" + gd.getDetails("s:hash");
			GlobalFileWriter.writeLineUnsync(line);
		}
		GlobalFileWriter.close();
	}

	@SuppressWarnings("unused")
	private void saveIndividualGraphs(Collection<GraphData> graphs, String suggestion, String title, Component parentComponent) {
		if (graphs.isEmpty()) {
			JOptionPane.showMessageDialog(parentComponent, "Nothing to save");
			return;
		}

		String prefix = (String) JOptionPane.showInputDialog(parentComponent, "Type the files' prefix", title, JOptionPane.PLAIN_MESSAGE, null, null, suggestion);

		if (prefix == null || prefix.trim().isEmpty())
			return;

		File folderFile = new File("output");
		if (!folderFile.exists()) {
			folderFile.mkdir();
		}
		if (folderFile.isDirectory()) {
			for (GraphData gd : graphs) {
				String filename = "output" + File.separator + prefix + "_" + gd.getId() + ".csv";
				try {
					gd.saveGraphCSV(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			JOptionPane.showConfirmDialog(parentComponent, "Could not create output directory", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public double getMinimumOfColumn(String column) {
		if (minimumOfColumn.containsKey(column))
			return minimumOfColumn.getDouble(column);

		double minimum = graphList.parallelStream().mapToDouble(graph -> Double.parseDouble(graph.getDetails(column)))//
				.min().getAsDouble();

		minimumOfColumn.put(column, minimum);
		return minimum;
	}

	public double getMaximumOfColumn(final String column) {
		if (maximumOfColumn.containsKey(column))
			return maximumOfColumn.getDouble(column);

		double maximum = graphList.parallelStream().mapToDouble(graph -> Double.parseDouble(graph.getDetails(column)))//
				.max().getAsDouble();

		maximumOfColumn.put(column, maximum);
		return maximum;
	}

	/**
	 * adapts the given value (must go from 0 to 100 (inclusive)) to be between the column's low and high range
	 * 
	 * @param column
	 * @param value
	 * @return
	 */
	public double getColumnAdaptedValue(String column, int value) {
		double low = getMinimumOfColumn(column);
		double range;
		lowHighColumnDifference = new Object2DoubleOpenHashMap<>();
		if (!lowHighColumnDifference.containsKey(column)) {
			double high = getMaximumOfColumn(column);
			range = high - low;
			lowHighColumnDifference.put(column, range);
		} else {
			range = lowHighColumnDifference.getDouble(column);
		}
		// double range=lowHighColumnDifference.g
		double res = low + (value / 100.0) * range;
		return res;
	}

	public void debugVisible() {
		System.out.println(visibleGraphList);
	}

	public void debugSelected() {
		System.out.println(sortToList(selectedGraphs));
	}

	public void debugDeleted() {
		System.out.println(sortToList(deletedGraphs));
	}

	private ArrayList<GraphData> sortToList(Collection<GraphData> c) {
		ArrayList<GraphData> list = new ArrayList<>(c);
		list.sort(new Comparator<GraphData>() {
			@Override
			public int compare(GraphData o1, GraphData o2) {
				int id1 = Integer.parseInt(o1.getId());
				int id2 = Integer.parseInt(o2.getId());
				return Integer.compare(id1, id2);
			}
		});
		return list;
	}

}
