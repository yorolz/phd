package structures;

import java.util.ArrayList;
import java.util.List;

public class UnorderedPair<T> {
	private T left;
	private T right;

	public UnorderedPair(T left, T right) {
		super();
		this.left = left;
		this.right = right;
	}

	public boolean containsElement(T element) {
		if (left.equals(element) || right.equals(element))
			return true;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		UnorderedPair<T> other = (UnorderedPair<T>) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}

	public List<T> getConcepts() {
		ArrayList<T> al = new ArrayList<>(2);
		al.add(left);
		al.add(right);
		return al;
	}

	public T getLeft() {
		return left;
	}

	public T getOpposingElement(T element) {
		if (left.equals(element))
			return right;
		if (right.equals(element))
			return left;
		return null;
	}

	public T getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		int result = left.hashCode() ^ right.hashCode();
		return result;
	}

}
