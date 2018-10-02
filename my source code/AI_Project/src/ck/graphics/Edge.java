package ck.graphics;

public class Edge {
	public int i0, i1;

	public Edge(int first, int second) {
		this.i0 = first;
		this.i1 = second;
	}

	public Edge reverse() {
		return new Edge(i1, i0);
	}

	public String toString() {
		return "[" + i0 + " " + i1 + "]";
	}
}
