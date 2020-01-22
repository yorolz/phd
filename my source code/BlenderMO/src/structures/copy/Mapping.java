package structures.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Mapping<T> {

	private T leftConcept;
	private T rightConcept;

	public Mapping(T leftConcept, T rightConcept) {
		super();
		this.leftConcept = leftConcept;
		this.rightConcept = rightConcept;
	}

	public boolean containsAnyConcept(Collection<T> concepts) {
		for (T concept : concepts) {
			if (leftConcept.equals(concept) || rightConcept.equals(concept))
				return true;
		}
		return false;
	}

	public boolean containsConcept(T concept) {
		if (leftConcept.equals(concept) || rightConcept.equals(concept))
			return true;
		return false;
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
		Mapping<T> other = (Mapping<T>) obj;
		if (other.hashCode() != this.hashCode())
			return false;

		if (leftConcept == null) {
			if (other.leftConcept != null)
				return false;
		}
		if (rightConcept == null) {
			if (other.rightConcept != null)
				return false;
		}

		// reversed analogy is the same
		if (leftConcept.equals(other.rightConcept) && rightConcept.equals(other.leftConcept))
			return true;

		if (!leftConcept.equals(other.leftConcept))
			return false;
		if (!rightConcept.equals(other.rightConcept))
			return false;
		return true;
	}

	public List<T> getConcepts() {
		ArrayList<T> al = new ArrayList<>();
		al.add(leftConcept);
		al.add(rightConcept);
		return al;
	}

	public T getLeftConcept() {
		return leftConcept;
	}

	public T getRightConcept() {
		return rightConcept;
	}

	@Override
	public int hashCode() {
		int h0 = leftConcept.hashCode();
		int h1 = rightConcept.hashCode();
		return h0 ^ h1;
	}

	public boolean isSelfMapping() {
		return leftConcept.equals(rightConcept);
	}

	public void setLeftConcept(T leftConcept) {
		this.leftConcept = leftConcept;
	}

	public void setRightConcept(T rightConcept) {
		this.rightConcept = rightConcept;
	}

	@Override
	public String toString() {
		return "(" + leftConcept + "," + rightConcept + ")";
	}

	public T getOpposingConcept(T concept) {
		if (leftConcept.equals(concept))
			return rightConcept;
		if (rightConcept.equals(concept))
			return leftConcept;
		return null;
	}
}
