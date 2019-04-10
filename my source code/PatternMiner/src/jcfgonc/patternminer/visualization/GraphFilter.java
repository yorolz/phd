package jcfgonc.patternminer.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import structures.GlobalFileWriter;

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
	private int numberVisibleGraphs;
	private boolean sortAscending;
	private GraphData currentlyClickedGD;
	private GraphData lastClickedGD;
	/**
	 * graphs which are currently selected (and highlighted)
	 */
	private HashSet<GraphData> selectedGraphs;
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

	public GraphFilter(String graphDatafile, int numberShownGraphs, MutableBoolean shiftKeyPressed) throws IOException {
		System.out.println("loading " + graphDatafile);
		this.originalGraphList = GraphData.createGraphsFromCSV("\t", new File(graphDatafile), true);
		System.out.format("creating %d graphs\n", originalGraphList.size());
		this.graphMap = new HashMap<>();
		this.shiftKeyPressed = shiftKeyPressed;
		this.visibleGraphList = new ArrayList<>();
		this.graphList = new ArrayList<>(originalGraphList);
		this.selectedGraphs = new HashSet<>();
		this.shiftSelectedGraphs = new HashSet<>();
		this.minimumOfColumn = new Object2DoubleOpenHashMap<>();
		this.maximumOfColumn = new Object2DoubleOpenHashMap<>();
		this.lowHighColumnDifference = new Object2DoubleOpenHashMap<>();
		this.columnFilterLow = new Object2DoubleOpenHashMap<>();
		this.columnFilterHigh = new Object2DoubleOpenHashMap<>();

		if (graphList.isEmpty())
			return;
		for (GraphData gd : originalGraphList) {
			addMouseClickHandler(gd);
			graphMap.put(gd.getId(), gd);
		}
		setNumberVisibleGraphs(numberShownGraphs);
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
						clearGraphCollection(shiftSelectedGraphs);
						selectedGraphs.removeAll(shiftSelectedGraphs);
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
		numberVisibleGraphs = num;
		// crop the list here
		updateVisibleList();
	}

	private void updateVisibleList() {
		visibleGraphList.clear();
		fillWithGraphs();
	}

	private void fillWithGraphs() {
		HashSet<GraphData> visibleGraphSet = new HashSet<>(visibleGraphList);
		Iterator<GraphData> graphListIterator = graphList.iterator();
		while (visibleGraphList.size() < numberVisibleGraphs && graphListIterator.hasNext()) {
			GraphData gd = graphListIterator.next();
			gd.getViewer().enableAutoLayout();
			if (visibleGraphSet.contains(gd))
				continue;
			visibleGraphList.add(gd);
			visibleGraphSet.add(gd);
		}
	}

	public void filterGraphs() {
		// TODO
		if (columnFilterLow.isEmpty() && columnFilterHigh.isEmpty())
			return;
		graphList.clear();
		outer: for (GraphData gd : originalGraphList) {
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
		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	public void setGraphFilter(String column, double lowValue, double highValue) {
		columnFilterLow.put(column, lowValue);
		columnFilterHigh.put(column, highValue);
	}

	public void sortGraphs(String columnName) {
		// sort the full list and then copy to the visible list
		graphList.sort(new Comparator<GraphData>() {

			@Override
			public int compare(GraphData o1, GraphData o2) {
				String d1 = o1.getDetails(columnName);
				String d2 = o2.getDetails(columnName);
				int comp;
				if (columnName.startsWith("n:")) {
					double v1 = Double.parseDouble(d1);
					double v2 = Double.parseDouble(d2);
					comp = Double.compare(v1, v2);
				} else { // assume it starts with "s:"
					comp = d1.compareTo(d2);
				}
				if (sortAscending) {
					return comp;
				} else {
					return -comp;
				}
			}
		});
		updateVisibleList();
		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	private void clearGraphCollection(Collection<GraphData> graphs) {
		if (graphs == null || graphs.isEmpty())
			return;
		for (GraphData graph : graphs) {
			graph.setSelected(false);
		}
	}

	public void debugButton() {
		System.out.println(selectedGraphs);
		System.out.println(visibleGraphList);
	}

	@SuppressWarnings("unused")
	private ArrayList<GraphData> sortToList(Collection<GraphData> c) {
		ArrayList<GraphData> list = new ArrayList<>(c);
		list.sort(new Comparator<GraphData>() {
			@Override
			public int compare(GraphData o1, GraphData o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		return list;
	}

	public void clearSelection() {
		for (GraphData graph : selectedGraphs) {
			graph.setSelected(false);
		}
		selectedGraphs.clear();
	}

	public void deleteSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// remove selection
		visibleGraphList.removeAll(selectedGraphs);
		for (GraphData gd : visibleGraphList) {
			gd.getViewer().disableAutoLayout();
		}
		graphList.removeAll(selectedGraphs);
		clearSelection();
		// fill with new graphs properly ordered
		fillWithGraphs();
		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	public void cropSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// delete all visible graphs which are not selected
		ArrayList<GraphData> toDelete = new ArrayList<>();
		for (GraphData gd : visibleGraphList) {
			if (!selectedGraphs.contains(gd)) {
				toDelete.add(gd);
			}
		}
		visibleGraphList.removeAll(toDelete);
		graphList.removeAll(toDelete);
		fillWithGraphs();
	}

	public void selectAllVisible() {
		selectedGraphs.clear();
		selectedGraphs.addAll(visibleGraphList);
		for (GraphData graph : visibleGraphList) {
			graph.setSelected(true);
		}
	}

	public void invertSelectionVisible() {
		for (GraphData graph : visibleGraphList) {
			toggleSelected(graph);
		}
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

	public void restoreDeletedGraphs() {
		// TODO Auto-generated method stub
	}

	public double getMinimumOfColumn(String column) {
		if (minimumOfColumn.containsKey(column))
			return minimumOfColumn.getDouble(column);
		double minimum = Double.MAX_VALUE;
		for (GraphData gd : graphList) {
			try {
				double val = Double.parseDouble(gd.getDetails(column));
				if (val < minimum)
					minimum = val;
			} catch (NumberFormatException e) {
				System.err.println("String column: " + column);
				e.printStackTrace();
			}

		}
		minimumOfColumn.put(column, minimum);
		return minimum;
	}

	public double getMaximumOfColumn(String column) {
		if (maximumOfColumn.containsKey(column))
			return maximumOfColumn.getDouble(column);
		double maximum = -Double.MAX_VALUE;
		for (GraphData gd : graphList) {
			try {
				double val = Double.parseDouble(gd.getDetails(column));
				if (val > maximum)
					maximum = val;
			} catch (NumberFormatException e) {
				System.err.println("String column: " + column);
				e.printStackTrace();
			}

		}
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
}
