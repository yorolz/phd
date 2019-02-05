package structures;

import java.util.ArrayList;
import java.util.List;

public class UnorderedPair<T> {
	private T left;
	private T right;

	public UnorderedPair(T left, T right) {
		super();
		if (left == null || right == null)
			throw new NullPointerException("both arguments can't be null");
		this.left = left;
		this.right = right;
	}

	public boolean containsElement(T element) {
		if (element == null)
			throw new NullPointerException("the function argument must not be null");
		if (left.equals(element) || right.equals(element))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result = left.hashCode() ^ right.hashCode();
		return result;
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
		if ((left.equals(other.left) && right.equals(other.right)) || // ----
				(left.equals(other.right) && right.equals(other.left))) {
			return true;
		}
		return false;
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
		if (element == null)
			throw new NullPointerException("the function argument must not be null");
		if (left.equals(element))
			return right;
		if (right.equals(element))
			return left;
		return null;
	}

	public T getRight() {
		return right;
	}

	public static <T> UnorderedPair<T> of(T left, T right) {
		return new UnorderedPair<>(left, right);
	}
}
