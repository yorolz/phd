package matcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AnalogySet {
	private Set<Mapping<String>> mappings;
	protected int hashcode;
	private boolean hashcodeSet;

	public AnalogySet() {
		super();
		this.hashcodeSet = false;
		this.mappings = new HashSet<>();
	}

	@Override
	public String toString() {
		return mappings.toString();
	}

	public AnalogySet(Set<Mapping<String>> mappings) {
		super();
		this.hashcodeSet = false;
		this.mappings = mappings;
	}

	public AnalogySet(Collection<Mapping<String>> mappings) {
		super();
		this.hashcodeSet = false;
		this.mappings = new HashSet<>(mappings);
	}

	public void add(Mapping<String> mapping) {
		mappings.add(mapping);
		hashcodeSet = false;
	}

	public void clear() {
		mappings.clear();
		hashcodeSet = false;
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
		hashcodeSet = false;
		return mappings.remove(o);
	}

	public void setMappings(HashSet<Mapping<String>> mappings) {
		this.mappings = mappings;
		hashcodeSet = false;
	}

	public int size() {
		return mappings.size();
	}

	public void writeMappings(BufferedWriter bw) throws IOException {
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

	public void writeMappings(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		writeMappings(bw);
		bw.close();
	}

	public void writeMappings(String filename) throws IOException {
		writeMappings(new File(filename));
	}

}
