package jcfgonc.patternminer.visualization;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class GraphFilter {
	private HashMap<String, GraphData> graphMap;
	private ArrayList<GraphData> visibleGraphList;
	private ArrayList<GraphData> graphList;
	private int numberGraphsToBeVisible;
	private boolean sortAscending;
	private GraphData clickedGD;
	private GraphData lastClicked;

	public GraphFilter(String graphDatafile, int numberShownGraphs) throws IOException {
		numberGraphsToBeVisible = numberShownGraphs;
		System.out.println("loading " + graphDatafile);
		graphList = GraphData.createGraphsFromCSV("\t", new File(graphDatafile), true);
		System.out.format("creating %d graphs\n", graphList.size());
		graphMap = new HashMap<>();
		visibleGraphList = new ArrayList<>();
		if (graphList.isEmpty())
			return;
		for (GraphData gd : graphList) {
//			DefaultView defaultView = gd.getDefaultView();
////			DefaultMouseManager manager = new DefaultMouseManager();
////			defaultView.setMouseManager(manager);
////			manager.release();
//

			gd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						String clickedComponent = e.getComponent().getName();
						lastClicked = clickedGD;
						clickedGD = getGraph(clickedComponent);
						clickedGD.toggleSelected();
						setGraphBorderState(lastClicked, false);
						setGraphBorderState(clickedGD, true);
					}
				}
			});
			graphMap.put(gd.getId(), gd);
		}
		setNumberVisibleGraphs(numberGraphsToBeVisible);
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
	 * returns the nth graph in the visible list
	 * 
	 * @param index
	 * @return
	 */
	public GraphData getVisibleGraph(int index) {
		return visibleGraphList.get(index);
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
		numberGraphsToBeVisible = num;
		// crop the list here
		copyFullToVisibleList();
	}

	private void copyFullToVisibleList() {
		visibleGraphList.clear();
		int maxGraphs = Math.min(numberGraphsToBeVisible, getNumberOfGraphs());
		for (int counter = 0; counter < maxGraphs; counter++) {
			GraphData gd = graphList.get(counter);
			visibleGraphList.add(gd);
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
		copyFullToVisibleList();
	}

	public void clearSelection() {
		for (GraphData graph : graphList) {
			graph.setSelected(false);
		}
	}

}
