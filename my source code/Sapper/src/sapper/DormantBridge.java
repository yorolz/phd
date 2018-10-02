package sapper;

/**
 * Represents a mapping (equivalence) between two concepts, both in the form of string.
 * 
 * @author ck
 *
 */
public class DormantBridge {

	private String concept0, concept1;
	private int hashcode;

	/**
	 * Creates a mapping between the two given concepts.
	 * 
	 * @param concept0
	 * @param concept1
	 */
	public DormantBridge(String concept0, String concept1) {
		super();
		this.concept0 = concept0;
		this.concept1 = concept1;

		// calculate hashcode and store
		this.hashcode = concept0.hashCode() + concept1.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		DormantBridge other = (DormantBridge) obj;

		if (this.concept0.equals(other.concept0) && (this.concept1.equals(other.concept1))) {
			return true;
		}

		// mappings may be revered, but it's the same
		if (this.concept0.equals(other.concept1) && (this.concept1.equals(other.concept0))) {
			return true;
		}

		return false;
	}

	public String getConcept0() {
		return concept0;
	}

	public String getConcept1() {
		return concept1;
	}

	public String toString() {
		return this.concept0 + "," + this.concept1;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

}
