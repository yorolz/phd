package jcfgonc.eemapper.structures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import graph.DirectedMultiGraph;
import graph.GraphReadWrite;

public class MappingStructure<V, E> {

	private DirectedMultiGraph<OrderedPair<V>, E> pairGraph;
	private OrderedPair<V> referencePair;

	public MappingStructure() {
		super();
		this.pairGraph = null;
		this.referencePair = null;
	}

	public Set<OrderedPair<V>> getMapping() {
		return pairGraph.vertexSet();
	}

	public DirectedMultiGraph<OrderedPair<V>, E> getPairGraph() {
		return pairGraph;
	}

	public OrderedPair<V> getRefPair() {
		return referencePair;
	}

	public void setPairGraph(DirectedMultiGraph<OrderedPair<V>, E> pairGraph) {
		this.pairGraph = pairGraph;
	}

	public void setRefPair(OrderedPair<V> refPair) {
		this.referencePair = refPair;
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

		// graph may not have been expanded, so there is only the reference pair
		if (getMapping().size() == 1) {
			V leftConcept = getRefPair().getLeftElement();
			V rightConcept = getRefPair().getRightElement();
			String r = String.format("m(%s,%s,%s).", "alignment", leftConcept.toString(), rightConcept.toString());
			bw.write(r);
			bw.newLine();
		} else { // pair graph was expanded, contains all the concept pairs
			for (OrderedPair<V> pair : getMapping()) {
				// m(alignment,horse,dragon).
				V leftConcept = pair.getLeftElement();
				V rightConcept = pair.getRightElement();
				String r = String.format("m(%s,%s,%s).", "alignment", leftConcept.toString(), rightConcept.toString());
				bw.write(r);
				bw.newLine();
			}
		}

		bw.flush();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		// go trough the pairs
		Iterator<OrderedPair<V>> iterator = getMapping().iterator();
		while (iterator.hasNext()) {
			OrderedPair<V> pair = iterator.next();
			s.append(pair.toString());
			// if there are more pairs, put them after a comma
			if (iterator.hasNext()) {
				s.append(',');
			}
		}
		return s.toString();
	}

	public void toString(BufferedWriter bw) throws IOException {
		Set<OrderedPair<V>> mapping = getMapping();

		// graph may not have been expanded, so there is only the reference pair
		if (mapping.size() == 1) {
			bw.write(getRefPair().toString());
		} else {

			// pair graph was expanded, contains all the concept pairs
			Iterator<OrderedPair<V>> iterator = mapping.iterator();
			while (iterator.hasNext()) {
				OrderedPair<V> pair = iterator.next();
				bw.write(pair.toString());
				// if there are more pairs, put them after a comma
				if (iterator.hasNext()) {
					bw.write(',');
				}
			}
		}
	}

}
