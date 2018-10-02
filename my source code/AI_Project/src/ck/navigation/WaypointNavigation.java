package ck.navigation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import ck.graphics.Edge;

import com.threed.jpct.SimpleVector;

public class WaypointNavigation {
	/**
	 * Hashtable with mapping of waypoint - connected waypoints. Uses Hashtable and HashSet for faster search and insertion
	 * operations.
	 */
	private Hashtable<Integer, HashSet<Integer>> neighborhoods;
	private Random random = new Random();

	/**
	 * Array of waypoints (Tridimensional vectors). The index is used in the paths.
	 */
	private ArrayList<SimpleCustomVector> waypoints;

	public WaypointNavigation(ArrayList<Edge> paths, ArrayList<SimpleCustomVector> waypoints) {
		this.waypoints = waypoints;
		buildNeighborhoods(paths);
	}

	private ArrayList<Integer> AStar(int start, int end) {
		Hashtable<Integer, Integer> camefrom = new Hashtable<Integer, Integer>();
		PriorityQueue<AStarNode> openset = new PriorityQueue<>();
		Hashtable<Integer, AStarNode> closedset = new Hashtable<Integer, AStarNode>();
		double rectilinearDistance = getRectilinearDistance(start, end);
		AStarNode first = new AStarNode(start, 0.0f, rectilinearDistance);
		openset.add(first);
		AStarNode current;
		int current_index = -1;
		while (openset.size() > 0) {
			current = (AStarNode) openset.poll();
			current_index = current.getIndex();
			if (current_index == end) {
				return (createPathAStarAsVector(current_index, start, camefrom));
			}
			closedset.put(current_index, current);
			HashSet<Integer> neighborhood = getNeighborhood(current_index);
			if (neighborhood != null && neighborhood.size() > 0) {
				for (int neighbor_index : neighborhood) {
					if (!closedset.containsKey(neighbor_index)) {
						double travelled_distance_start = (double) (current.getTravelledDistanceFromStart() + getRectilinearDistance(
								neighbor_index, current_index));
						double estimated_distance_goal = getRectilinearDistance(neighbor_index, end);
						// check if neighbor node is on openset
						AStarNode neighbor_node = getNode(openset, neighbor_index);
						if (neighbor_node != null) {
							double last_travelled_distance_start = neighbor_node.getTravelledDistanceFromStart();
							// current visit better?
							if (travelled_distance_start < last_travelled_distance_start) {
								// neigbor visited early with greater distance
								openset.remove(neighbor_node);
								AStarNode node = new AStarNode(neighbor_index, travelled_distance_start,
										estimated_distance_goal);
								openset.add(node);
								camefrom.put(neighbor_index, current_index);
							} else {
								// neighbor visited early with shorter distance
							}
						} else {
							// neighbor not yet visited, add to openset
							AStarNode node = new AStarNode(neighbor_index, travelled_distance_start, estimated_distance_goal);
							openset.add(node);
							camefrom.put(neighbor_index, current_index);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Digests the constructor's path ArrayListing into a set of points and connected neighbors, for each point.
	 */
	private void buildNeighborhoods(ArrayList<Edge> edges) {
		neighborhoods = new Hashtable<Integer, HashSet<Integer>>();
		HashSet<Integer> connected;
		// cycle paths
		for (Edge edge : edges) {
			// create paths for both wp and second wp
			// get mapping for first wp
			connected = neighborhoods.get(edge.i0);
			// no ArrayList of connected wp exists, create new ArrayList
			if (connected == null) {
				connected = new HashSet<Integer>();
			}
			// add current neighbor
			connected.add(edge.i1);
			neighborhoods.put(edge.i0, connected);
			// get mapping for second wp
			connected = neighborhoods.get(edge.i1);
			// no ArrayList of connected wp exists, create new ArrayList
			if (connected == null) {
				connected = new HashSet<Integer>();
			}
			// add current neighbor
			connected.add(edge.i0);
			neighborhoods.put(edge.i1, connected);
		}
		System.out.println("Waypoints:" + waypoints.size() + " Edges:" + edges.size());
	}

	private ArrayList<Integer> createPathAStarAsVector(int current_index, int start, Hashtable<Integer, Integer> camefrom) {
		ArrayList<Integer> ll = new ArrayList<Integer>();
		ll.add(current_index);
		while (current_index != start) {
			// System.out.println(current_index);
			current_index = camefrom.get(current_index);
			ll.add(0, current_index);
		}
		return ll;
	}

	/**
	 * Finds the nearest waypoint to the given position, within a specified tolerance
	 * 
	 * @param position
	 *            the position to get the nearest waypoint
	 * @param precision
	 *            the maximum distance allowed between the position and the waypoint
	 * @return index of the nearest waypoint on the waypoint's ArrayList
	 */
	private int getNearestWaypoint(SimpleVector position) {
		SimpleCustomVector testPosition = null;
		int nearesti = -1;
		double currentdist = 0;
		double nearestdist = Double.MAX_VALUE;
		int index = 0;
		while (index < waypoints.size()) {
			testPosition = getWaypoint(index);
			currentdist = testPosition.distanceAbsolute(position);
			if (currentdist < nearestdist) {
				nearestdist = currentdist;
				nearesti = index;
			}
			index++;
		}
		return nearesti;
	}

	/**
	 * Returns the ArrayList of connected waypoints to the given one
	 * 
	 * @param index
	 *            the waypoint to get the neighborhood from
	 * @return the set of indexes containing the neighborhood
	 */
	private HashSet<Integer> getNeighborhood(int index) {
		return neighborhoods.get(index);
	}

	private AStarNode getNode(PriorityQueue<AStarNode> set, int waypoint_index) {
		for (AStarNode object : set) {
			if (object.getIndex() == waypoint_index)
				return object;
		}
		return null;
	}

	public List<SimpleVector> getPathAsArrayList(SimpleVector source, SimpleVector target) {
		int sourcei = getNearestWaypoint(source);
		int targeti = getNearestWaypoint(target);
		ArrayList<Integer> astar = AStar(sourcei, targeti);
		ArrayList<SimpleVector> path = new ArrayList<SimpleVector>();
		if (astar != null && astar.size() > 0) {
			for (int i : astar) {
				path.add(getWaypoint(i));
			}
		}
		// because the destination may be different than the last waypoint, add it to the path
		if (path.get(path.size() - 1).distance(target) > 0.0001)
			path.add(target);
		return path;
	}

	public SimpleVector getRandomWaypoint() {
		int randomIndex = random.nextInt(waypoints.size());
		SimpleCustomVector waypoint = getWaypoint(randomIndex);
		return waypoint;
	}

	private double getRectilinearDistance(int start, int end) {
		return getWaypoint(start).distanceAbsolute(getWaypoint(end));
	}

	/**
	 * Returns the waypoint at the specified index
	 * 
	 * @param index
	 *            the position on the waypoint ArrayList
	 * @return the waypoint at the given index
	 */
	private SimpleCustomVector getWaypoint(int index) {
		return waypoints.get(index);
	}

}
