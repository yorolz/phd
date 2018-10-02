package jcfgonc.eemapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import graph.DirectedMultiGraph;
import graph.GraphEdge;
import graph.GraphReadWrite;
import graph.StringGraph;
import mapper.OrderedPair;

public class MappingStructure<V, E> {

	private DirectedMultiGraph<OrderedPair<V>, E> pairGraph;
	private OrderedPair<V> refPair;

	public MappingStructure() {
		super();
		this.pairGraph = null;
		this.refPair = null;
	}

	public Set<OrderedPair<V>> getMapping() {
		return pairGraph.vertexSet();
	}

	public DirectedMultiGraph<OrderedPair<V>, E> getPairGraph() {
		return pairGraph;
	}

	public OrderedPair<V> getRefPair() {
		return refPair;
	}

	public void setPairGraph(DirectedMultiGraph<OrderedPair<V>, E> pairGraph) {
		this.pairGraph = pairGraph;
	}

	public void setRefPair(OrderedPair<V> refPair) {
		this.refPair = refPair;
	}

	public void writeTGF(String filename) throws IOException {
		GraphReadWrite.writeTGF(filename, pairGraph);
	}

	public String writeMappingDivago() throws IOException {
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		writeMappingDivago(bw);
		return sw.toString();
	}

	public void writeMappingDivago(BufferedWriter bw) throws IOException {
		bw.write(":-multifile m/3.");
		bw.newLine();
		bw.newLine();

		for (OrderedPair<V> pair : getMapping()) {
			// m(alignment,horse,dragon).
			V leftConcept = pair.getLeftElement();
			V rightConcept = pair.getRightElement();
			String r = String.format("m(%s,%s,%s).", "alignment", leftConcept.toString(), rightConcept.toString());
			bw.write(r);
			bw.newLine();
		}
		bw.flush();
	}

	public String writePairGraphDT() throws IOException {
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		writePairGraphDT(bw);
		return sw.toString();
	}

	public void writePairGraphDT(BufferedWriter bw) throws IOException {
		StringGraph g = new StringGraph();
		for (GraphEdge<OrderedPair<V>, E> edge : pairGraph.edgeSet()) {
			String edgeSource = edge.getSource().toString();
			String edgeTarget = edge.getTarget().toString();
			String edgeLabel = edge.getLabel().toString();

			g.addEdge(edgeSource, edgeTarget, edgeLabel);
		}
		GraphReadWrite.writeDT(bw, g, "mapping");
	}

}
