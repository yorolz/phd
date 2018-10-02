package ck.navigation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ck.graphics.Edge;
import ck.ia.Config;

import com.threed.jpct.Polyline;
import com.threed.jpct.World;

public class WaypointCreator {

	private World world;
	private double delta = Config.WAYPOINT_GRID_DELTA;
	private SimpleCustomVector startingPosition;
	private ArrayList<Edge> lines;
	private ArrayList<SimpleCustomVector> vertices;
	private HashMap<SimpleCustomVector, Integer> vecToID;
	private World wireframeWorld;

	public WaypointCreator(World world, SimpleCustomVector testPosition, World wireframeWorld) {
		this.world = world;
		this.startingPosition = testPosition;
		this.wireframeWorld = wireframeWorld;
	}

	private int ipow(int base, int exp) {
		if (exp == 0)
			return 1;
		else if (exp == 1)
			return base;
		else if (base == 2) {
			return 1 << exp;
		} else {
			int result = 1;
			while (exp > 0) {
				if ((exp & 1) > 0)
					result *= base;
				exp >>= 1;
				base *= base;
			}
			return result;
		}
	}

	public void createWaypoints() {

		ArrayList<SimpleCustomVector> directions = new ArrayList<>();
		// cross
		directions.add(new SimpleCustomVector(+1, 0, 0));
		directions.add(new SimpleCustomVector(-1, 0, 0));
		directions.add(new SimpleCustomVector(0, 0, +1));
		directions.add(new SimpleCustomVector(0, 0, -1));
		// diagonal
		final int minlevel = 0;
		final int maxlevel = 0;
		for (int l = minlevel; l <= maxlevel; l++) {
			directions.add(new SimpleCustomVector(+ipow(2, l), 0, +1));
			directions.add(new SimpleCustomVector(+ipow(2, l), 0, -1));
			directions.add(new SimpleCustomVector(-ipow(2, l), 0, +1));
			directions.add(new SimpleCustomVector(-ipow(2, l), 0, -1));
			directions.add(new SimpleCustomVector(+1, 0, +ipow(2, l)));
			directions.add(new SimpleCustomVector(+1, 0, -ipow(2, l)));
			directions.add(new SimpleCustomVector(-1, 0, +ipow(2, l)));
			directions.add(new SimpleCustomVector(-1, 0, -ipow(2, l)));
		}

		HashSet<SimpleCustomVector> openSet = new HashSet<>();
		HashSet<SimpleCustomVector> closedSet = new HashSet<>();
		openSet.add(startingPosition);

		this.lines = new ArrayList<>();
		this.vertices = new ArrayList<>();
		this.vecToID = new HashMap<>();

		int counter = 0;

		while (!openSet.isEmpty()) {
			if ((counter & 255) == 0)
				System.out.println("explored nodes: " + closedSet.size() + " nodes to be explored: " + openSet.size());

			SimpleCustomVector currentNode = openSet.iterator().next();
			openSet.remove(currentNode);
			closedSet.add(currentNode);

			// expand cross
			// check movement in each direction (maximum length is delta)
			{
				for (SimpleCustomVector direction : directions) {
					SimpleCustomVector position = new SimpleCustomVector(currentNode.x + delta * direction.x, currentNode.y
							+ delta * direction.y, currentNode.z + delta * direction.z);
					if (!closedSet.contains(position)) {
						float distance = world.calcMinDistance(currentNode, direction, Float.MAX_VALUE);
						if (distance > delta) {
							openSet.add(position);
							addWaypointLine(currentNode, position);
						}
					}
				}
			}

		}
		System.out.println("explored nodes: " + closedSet.size() + " nodes to be explored: " + openSet.size());
	}

	private void addWaypointLine(SimpleCustomVector v0, SimpleCustomVector v1) {
		if (!vecToID.containsKey(v0)) {
			vecToID.put(v0, vecToID.size());
			this.vertices.add(v0);
		}
		if (!vecToID.containsKey(v1)) {
			vecToID.put(v1, vecToID.size());
			this.vertices.add(v1);
		}
		Integer vi0 = vecToID.get(v0);
		Integer vi1 = vecToID.get(v1);
		Edge line = new Edge(vi0, vi1);
		this.lines.add(line);

		SimpleCustomVector[] points = { v0, v1 };
		Polyline pl = new Polyline(points, Color.RED);
		wireframeWorld.addPolyline(pl);

	}

	public ArrayList<Edge> getLines() {
		return this.lines;
	}

	public ArrayList<SimpleCustomVector> getVertices() {
		return this.vertices;
	}

}
