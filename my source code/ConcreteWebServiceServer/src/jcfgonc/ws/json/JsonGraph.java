package jcfgonc.ws.json;

import java.util.List;

public class JsonGraph {
	public boolean directed; // true
	public List<List<String>> graph; // irrelevant
	public List<Node> nodes; // contains the vertices
	public List<Link> links; // contains the edges
	public boolean multigraph; // true
}
