package structures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A Map where each mapping is a Set of the given type, mapped to by a key. Created for convenience.
 * 
 * @author Jo�o Gon�alves: jcfgonc@gmail.com
 * @param <K>
 * @param <V>
 */
public class MapOfSet<K, V> {

	private HashMap<K, Set<V>> map;

	public MapOfSet(int a) {
		map = new HashMap<K, Set<V>>(a);
	}

	public MapOfSet() {
		map = new HashMap<K, Set<V>>();
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public Set<V> get(K key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Set<V> mergeSets() {
		HashSet<V> merged = new HashSet<>();
		Collection<Set<V>> vSet = map.values();
		for (Set<V> set : vSet) {
			merged.addAll(set);
		}
		return merged;
	}

	public boolean put(K key, V value) {
		Set<V> set = get(key);
		if (set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		return set.add(value);
	}

	public void put(K key, Collection<V> values) {
		for (V value : values) {
			put(key, value);
		}
	}

	public void removeFromValues(Collection<V> values) {
		for (V value : values) {
			removeFromValues(value);
		}
	}

	public void removeFromValues(V value) {
		// iterate through every mapped set (target)
		for (Set<V> values : this.values()) {
			values.remove(value);
		}
	}

	public Set<V> removeKey(K key) {
		return map.remove(key);
	}

	public int size() {
		return map.size();
	}

	public String toString() {
		return map.toString();
	}

	public Collection<Set<V>> values() {
		return map.values();
	}

}
