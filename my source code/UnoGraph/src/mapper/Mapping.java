package mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class Mapping<E> {
	public static Mapping<String> readMappingDT(String filename) throws IOException {
		Mapping<String> analogy = new Mapping<String>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while (br.ready()) {
			String line = br.readLine();
			if (line == null)
				break;
			line = line.trim();
			// ignore empty lines
			if (line.length() == 0)
				continue;
			// ignore commented lines
			if (line.startsWith(":-"))
				continue;
			// get text after the domain and before the ending parentheses
			String cleaned = line.substring(line.indexOf(",") + 1, line.lastIndexOf(")"));
			StringTokenizer st = new StringTokenizer(cleaned, ",");

			String leftElement = st.nextToken().trim();
			String rightElement = st.nextToken().trim();

			if (leftElement.equals(rightElement)) {
				System.out.println("LOOP: " + line);
				continue;
			}
			OrderedPair<String> mapping = new OrderedPair<String>(leftElement, rightElement);
			analogy.add(mapping);
		}
		br.close();
		return analogy;
	}

	public static List<Mapping<String>> readMappingTXT(String filename) throws IOException {
		ArrayList<Mapping<String>> analogies = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		Mapping<String> currentAnalogy = new Mapping<String>();
		analogies.add(currentAnalogy);
		while (br.ready()) {
			String line = br.readLine().trim();
			if (line.startsWith("-")) {
				currentAnalogy = new Mapping<String>();
				analogies.add(currentAnalogy);
				continue;
			}
			String[] tokens = line.split("\\s");
			if (tokens.length == 2) {
				String leftElement = tokens[0];
				String rightElement = tokens[1];
				OrderedPair<String> mapping = new OrderedPair<String>(leftElement, rightElement);
				currentAnalogy.add(mapping);
			}
		}
		br.close();
		return analogies;
	}

	/**
	 * the pairs as a set (for quick ops)
	 */
	private HashSet<OrderedPair<E>> pairs;

	public Mapping() {
		super();
		pairs = new HashSet<>();
	}

	public Mapping(Collection<OrderedPair<E>> other) {
		super();
		pairs = new HashSet<>(other);
	}

	public Mapping(Mapping<E> other) {
		super();
		pairs = new HashSet<>(other.pairs);
	}

	public boolean add(E left, E right) {
		OrderedPair<E> mapping = new OrderedPair<>(left, right);
		boolean add = pairs.add(mapping);
		return add;
	}

	public boolean add(OrderedPair<E> mapping) {
		boolean add = pairs.add(mapping);
		return add;
	}

	public void clear() {
		pairs.clear();
	}

	public boolean contains(Object o) {
		return pairs.contains(o);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Mapping<E> other = (Mapping<E>) obj;
		if (pairs == null) {
			if (other.pairs == null)
				return true;
			else
				return false;
		} else if (pairs != null) {
			if (other.pairs == null)
				return false;
			else {
				if (other.hashCode() != this.hashCode())
					return false;
				else
					return this.pairs.equals(other.pairs);
			}
		}
		return false;

	}

	public HashSet<OrderedPair<E>> getOrderedPairs() {
		return pairs;
	}

	@Override
	public int hashCode() {
		return pairs.hashCode();
	}

	public boolean isEmpty() {
		return pairs.isEmpty();
	}

	public boolean remove(Object o) {
		return pairs.remove(o);
	}

	public int size() {
		return pairs.size();
	}

	@Override
	public String toString() {
		return pairs.toString();
	}

	public void writeMappingDT(BufferedWriter bw) throws IOException {
		bw.write(":-multifile m/3.");
		bw.newLine();
		bw.newLine();

		for (OrderedPair<E> m : pairs) {
			// m(alignment,horse,dragon).
			E leftElement = m.getLeftElement();
			E rightElement = m.getRightElement();
			String r = String.format("m(%s,%s,%s).", "alignment", leftElement, rightElement);
			bw.write(r);
			bw.newLine();
		}
		bw.flush();
	}

	public void writeMappingDT(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		writeMappingDT(bw);
		bw.close();
	}

	public void writeMappingDT(String filename) throws IOException {
		writeMappingDT(new File(filename));
	}

}
