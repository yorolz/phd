package jcfgonc.patternminer.visualization;

import java.awt.Color;
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

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.mutable.MutableBoolean;

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

		if (graphList.isEmpty())
			return;
		for (GraphData gd : graphList) {
			addMouseClickHandler(gd);
			graphMap.put(gd.getId(), gd);
		}
		setNumberVisibleGraphs(numberShownGraphs);
	}

	private void addMouseClickHandler(GraphData gd) {
		gd.addMouseListener(new MouseAdapter() {
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
		});
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
	}

	private void clearGraphCollection(Collection<GraphData> graphs) {
		if (graphs == null || graphs.isEmpty())
			return;
		for (GraphData graph : graphs) {
			graph.setSelected(false);
		}
	}

	public void debugButton() {
		System.out.println(sortList(selectedGraphs));
		System.out.println(sortList(visibleGraphList));
	}

	private ArrayList<GraphData> sortList(Collection<GraphData> c) {
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

	public void restoreDeletedGraphs() {
		// TODO Auto-generated method stub

	}

	public void cropSelection() {
		// TODO Auto-generated method stub

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

}
