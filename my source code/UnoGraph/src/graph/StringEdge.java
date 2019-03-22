package graph;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringEdge implements Comparable<StringEdge>, Serializable, Cloneable {
	private static final long serialVersionUID = 8432349686429608349L;
	private String source;
	private String target;
	private String label;
	private int hashcode;

	public StringEdge(String source, String target, String label) {
		this.source = source;
		this.target = target;
		this.label = label;
		cacheHashCode();
	}

	public StringEdge(StringEdge other) {
		this(other.source, other.target, other.label);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new StringEdge(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringEdge other = (StringEdge) obj;
		return label.equals(other.label) && // ---
				source.contentEquals(other.source) && // ---
				target.contentEquals(other.target);
	}

	public String getLabel() {
		return label;
	}

	public String getOppositeOf(String v) {
		if (source.equals(v)) {
			return target;
		} else if (target.equals(v)) {
			return source;
		} else {
			return null;
		}
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	private void cacheHashCode() {
		final int prime = 127;
		hashcode = 1;
		hashcode = prime * hashcode + ((label == null) ? 0 : label.hashCode());
		hashcode = prime * hashcode + ((source == null) ? 0 : source.hashCode());
		hashcode = prime * hashcode + ((target == null) ? 0 : target.hashCode());
	}

	/**
	 * Returns a more accurate hash code of this StringEdge.
	 * 
	 * @return
	 */
	public byte[] getHashedBytes() {
		byte[] byteArray = getBytes();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException nsae) {
			throw new InternalError("SHA-256 not supported", nsae);
		}
		byte[] d = md.digest(byteArray);
		return d;
	}

	public byte[] getBytes() {
		String merge = label + "\0" + source + "\0" + target + "\0";
		byte[] byteArray = merge.getBytes(Charset.forName("UTF-8"));
		return byteArray;
	}

	public boolean incomesTo(String reference) {
		if (target.equals(reference)) {
			return true;
		} else
			return false;
	}

	public boolean outgoesFrom(String reference) {
		if (source.equals(reference)) {
			return true;
		} else
			return false;
	}

	/**
	 * Creates a new StringEdge with the given oldReference (vertex) replaced with the newReference (vertex). Both source and target may be replaced. VALIDATED.
	 *
	 * @param oldReference
	 * @param newReference
	 * @return
	 */
	public StringEdge replaceSourceOrTarget(String oldReference, String newReference) {
		StringEdge newEdge = new StringEdge(this);
		if (oldReference.equals(newReference)) // do nothing is this case, duh
			return newEdge;
		if (newEdge.source.equals(oldReference)) {
			newEdge.source = newReference;
		}
		if (newEdge.target.equals(oldReference)) {
			newEdge.target = newReference;
		}
		return newEdge;
	}

	public StringEdge replaceLabel(String oldLabel, String newLabel) {
		StringEdge newEdge = new StringEdge(this);
		if (oldLabel.equals(newLabel)) // do nothing is this case, duh
			return newEdge;
		if (newEdge.label.equals(oldLabel)) {
			newEdge.label = newLabel;
		}
		return newEdge;
	}

	/**
	 * Returns a new edge with the same relation and reversed source / target vertices.
	 * 
	 * @return
	 */
	public StringEdge reverse() {
		return new StringEdge(target, source, label);
	}

	public String toString() {
		return source + "," + label + "," + target;
	}

	public boolean isLoop() {
		return source.equals(target);
	}

	public boolean containsConcept(String reference) {
		if (source.equals(reference) || target.equals(reference))
			return true;
		else
			return false;
	}

	@Override
	public int compareTo(StringEdge o) {
		// return compareToSTL(o);
		return compareToTSL(o);
		// return compareToLST(o);
	}

	public int compareToSTL(StringEdge o) {
		int comp;
		if ((comp = source.compareTo(o.source)) != 0)
			return comp;
		else {
			if ((comp = target.compareTo(o.target)) != 0)
				return comp;
			else {
				return label.compareTo(o.label);
			}
		}
	}

	public int compareToTSL(StringEdge o) {
		int comp;
		if ((comp = target.compareTo(o.target)) != 0)
			return comp;
		else {
			if ((comp = source.compareTo(o.source)) != 0)
				return comp;
			else {
				return label.compareTo(o.label);
			}
		}
	}

	public int compareToLST(StringEdge o) {
		int comp;
		if ((comp = label.compareTo(o.label)) != 0)
			return comp;
		else {
			if ((comp = source.compareTo(o.source)) != 0)
				return comp;
			else {
				return target.compareTo(o.target);
			}
		}
	}

	public String getCommonConcept(StringEdge edge) {
		if (this.containsConcept(edge.source))
			return edge.source;
		if (this.containsConcept(edge.target))
			return edge.target;
		return null;
	}
}
