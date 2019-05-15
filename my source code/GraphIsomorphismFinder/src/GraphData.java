import graph.StringGraph;

public class GraphData {

	private String id;
	private StringGraph graph;

	public GraphData(String id, StringGraph graph) {
		this.id = id;
		this.graph = graph;
	}

	public String getId() {
		return id;
	}

	public StringGraph getGraph() {
		return graph;
	}

}
