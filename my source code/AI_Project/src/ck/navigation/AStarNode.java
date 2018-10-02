package ck.navigation;

public class AStarNode implements Comparable<AStarNode> {
	private static final int debuglevel = 0;
	private double estimated_distance_goal;
	private double heuristic;
	private int index;
	private double travelled_distance_start;

	public AStarNode(int index, double travelled_distance_start, double estimated_distance_goal) {
		this.travelled_distance_start = travelled_distance_start;
		this.estimated_distance_goal = estimated_distance_goal;
		this.heuristic = travelled_distance_start + estimated_distance_goal;
		this.index = index;
	}

	public int compareTo(AStarNode o) {
		if (o.heuristic > this.heuristic)
			return -1;
		else if (o.heuristic < this.heuristic) {
			return +1;
		} else
			return 0;
	}

	public boolean equals(AStarNode node) {
		return node.index == this.index;
	}

	public double getEstimatedDistanceToGoal() {
		return estimated_distance_goal;
	}

	public double getHeuristic() {
		return heuristic;
	}

	public int getIndex() {
		return index;
	};

	public double getTravelledDistanceFromStart() {
		return travelled_distance_start;
	}

	public int hashCode() {
		return this.getIndex();
	}

	public String toString() {
		switch (AStarNode.debuglevel) {
		case 0:
			return "(" + "heuristic=" + this.heuristic + ",index=" + this.index + ",estimated_distance_goal="
					+ this.estimated_distance_goal + ",travelled_distance_start=" + this.travelled_distance_start + ")";
		case 1:
			return "(" + "heuristic=" + this.heuristic + ")";
		case 2:
			return "(" + "heuristic=" + this.heuristic + ",index=" + this.index + ")";
		}
		return "";
	}
}
