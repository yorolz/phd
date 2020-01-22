package structures.copy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class AnalogySet {
	public static AnalogySet readMappingsDT(File file) throws IOException {
		AnalogySet analogy = new AnalogySet();
		BufferedReader br = new BufferedReader(new FileReader(file));
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

			String leftConcept = st.nextToken().trim();
			String rightConcept = st.nextToken().trim();

			if (leftConcept.equals(rightConcept)) {
				System.out.println("LOOP: " + line);
				continue;
	 		}
			Mapping<String> mapping = new Mapping<String>(leftConcept, rightConcept);
			analogy.add(mapping);
		}
		br.close();
		return analogy;
	}
	public static AnalogySet readMappingsDT(String filename) throws IOException {
		return readMappingsDT(new File(filename));
	}
	public static List<AnalogySet> readMappingsTXT(String filename) throws IOException {
		ArrayList<AnalogySet> analogies = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		AnalogySet currentAnalogy = new AnalogySet();
		analogies.add(currentAnalogy);
		while (br.ready()) {
			String line = br.readLine().trim();
			if (line.startsWith("-")) {
				currentAnalogy = new AnalogySet();
				analogies.add(currentAnalogy);
				continue;
			}
			String[] tokens = line.split("\\s");
			if (tokens.length == 2) {
				String leftConcept = tokens[0];
				String rightConcept = tokens[1];
				Mapping<String> mapping = new Mapping<String>(leftConcept, rightConcept);
				currentAnalogy.add(mapping);
			}
		}
		br.close();
		return analogies;
	}

	protected int hashcode;

	private boolean hashcodeSet;

	private Set<Mapping<String>> mappings;

	public AnalogySet() {
		super();
		this.hashcodeSet = false;
		this.mappings = new HashSet<>();
	}

	public AnalogySet(Collection<Mapping<String>> mappings) {
		super();
		this.hashcodeSet = false;
		this.mappings = new HashSet<>(mappings);
	}

	public AnalogySet(Set<Mapping<String>> mappings) {
		super();
		this.hashcodeSet = false;
		this.mappings = mappings;
	}

	public void add(Mapping<String> mapping) {
		mappings.add(mapping);
	}

	public void clear() {
		mappings.clear();
	}

	public boolean contains(Object o) {
		return mappings.contains(o);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalogySet other = (AnalogySet) obj;
		if (mappings == null) {
			if (other.mappings == null)
				return true;
			else
				return false;
		} else if (mappings != null) {
			if (other.mappings == null)
				return false;
			else {
				if (other.hashCode() != this.hashCode())
					return false;
				else
					return this.mappings.equals(other.mappings);
			}
		}
		return false;

	}

	public Mapping<String> getMapping(int index){
		Iterator<Mapping<String>> iter = this.iterator();
		for (int i = 0; i < index; i++) {
		    iter.next();
		}
		return iter.next();
	}

	public Set<Mapping<String>> getMappings() {
		return mappings;
	}

	@Override
	public int hashCode() {
		if (!hashcodeSet) {
			hashcode = mappings.hashCode();
			hashcodeSet = true;
		}
		return hashcode;
	}

	public boolean isEmpty() {
		return mappings.isEmpty();
	}
	
	public Iterator<Mapping<String>> iterator() {
		return mappings.iterator();
	}

	public boolean remove(Object o) {
		return mappings.remove(o);
	}

	public int size() {
		return mappings.size();
	}

	@Override
	public String toString() {
		return mappings.toString();
	}

	public void writeMappingsDT(BufferedWriter bw) throws IOException {
		bw.write(":-multifile m/3.");
		bw.newLine();
		bw.newLine();

		for (Mapping<String> m : mappings) {
			// m(alignment,horse,dragon).
			String leftConcept = m.getLeftConcept();
			String rightConcept = m.getRightConcept();
			String r = String.format("m(%s,%s,%s).", "alignment", leftConcept, rightConcept);
			bw.write(r);
			bw.newLine();
		}
		bw.flush();
	}

	public void writeMappingsDT(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		writeMappingsDT(bw);
		bw.close();
	}

	public void writeMappingsDT(String filename) throws IOException {
		writeMappingsDT(new File(filename));
	}

}
