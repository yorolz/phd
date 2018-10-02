package mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrderedPair<T> {
	private int cachedHashCode;
	private T leftElement;
	private T rightElement;

	public OrderedPair(OrderedPair<T> other) {
		super();
		this.leftElement = other.leftElement;
		this.rightElement = other.rightElement;
		precacheHashCode();
	}

	public OrderedPair(T leftElement, T rightElement) {
		super();
		this.leftElement = leftElement;
		this.rightElement = rightElement;
		precacheHashCode();
	}

	public boolean containsAnyElement(Collection<T> elements) {
		for (T element : elements) {
			if (leftElement.equals(element) || rightElement.equals(element))
				return true;
		}
		return false;
	}

	public boolean containsElement(T element) {
		if (leftElement.equals(element) || rightElement.equals(element))
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
		OrderedPair<T> other = (OrderedPair<T>) obj;
		int ohc = other.hashCode();
		int thc = this.hashCode();
		if (ohc != thc)
			return false;

		if (leftElement == null) {
			if (other.leftElement != null)
				return false;
		}
		if (rightElement == null) {
			if (other.rightElement != null)
				return false;
		}

		// reversed analogy is the same
		if (leftElement.equals(other.rightElement) && rightElement.equals(other.leftElement))
			return true;

		if (!leftElement.equals(other.leftElement))
			return false;
		if (!rightElement.equals(other.rightElement))
			return false;
		return true;
	}

	public List<T> getElements() {
		ArrayList<T> al = new ArrayList<>();
		al.add(leftElement);
		al.add(rightElement);
		return al;
	}

	public T getLeftElement() {
		return leftElement;
	}

	public T getOpposingElement(T element) {
		if (leftElement.equals(element))
			return rightElement;
		if (rightElement.equals(element))
			return leftElement;
		return null;
	}

	public T getRightElement() {
		return rightElement;
	}

	@Override
	public int hashCode() {
		return cachedHashCode;
	}

	public boolean isSelfMapping() {
		return leftElement.equals(rightElement);
	}

	private void precacheHashCode() {
		int h0 = leftElement.hashCode();
		int h1 = rightElement.hashCode();
		cachedHashCode = h0 ^ h1;
	}

	public void setElements(T leftElement, T rightElement) {
		this.leftElement = leftElement;
		this.rightElement = rightElement;
		precacheHashCode();
	}

	public void setLeftElement(T leftElement) {
		this.leftElement = leftElement;
		precacheHashCode();
	}

	public void setRightElement(T rightElement) {
		this.rightElement = rightElement;
		precacheHashCode();
	}

	@Override
	public String toString() {
		return leftElement + "<=>" + rightElement;
	}
}
