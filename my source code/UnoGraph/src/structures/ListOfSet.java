package structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An array where each element is a set of the given type. Created for convenience.
 * 
 * @author Joao Goncalves: jcfgonc@gmail.com
 *
 */
public class ListOfSet<E> implements Iterable<HashSet<E>>{

	private ArrayList<HashSet<E>> array;

	public ListOfSet() {
		array = new ArrayList<>();
	}

	public HashSet<E> createSetAt(int pos) {
		fillTo(pos);
		HashSet<E> set = getSetAt(pos);
		return set;
	}

	private void fillTo(int pos) {
		int size = this.size();
		for (int i = size; i <= pos; i++) {
			HashSet<E> existing = getSetAt(i);
			if (existing == null) {
				existing = new HashSet<>();
				array.add(existing);
			}
		}
	}

	public boolean addElementToSetAt(E e, int pos) {
		HashSet<E> set = this.getSetAt(pos);
		if (set == null)
			set = createSetAt(pos);
		return set.add(e);
	}

	public boolean add(HashSet<E> e) {
		return array.add(e);
	}

	public boolean contains(Object o) {
		return array.contains(o);
	}

	public HashSet<E> getSetAt(int index) {
		if (index >= array.size())
			return null;
		return array.get(index);
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public Iterator<HashSet<E>> iterator() {
		return array.iterator();
	}

	public int size() {
		return array.size();
	}

	public String toString() {
		return array.toString();
	}

	public void sortList(boolean ascending) {
		array.sort(new Comparator<HashSet<E>>() {

			@Override
			public int compare(HashSet<E> o1, HashSet<E> o2) {
				if (ascending) {
					return Integer.compare(o1.size(), o2.size());
				} else {
					return Integer.compare(o2.size(), o1.size());
				}
			}

		});
	}

}
