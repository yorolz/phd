package structures;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Maps/indexes objects (according to their hashcode) to unique identifiers (numbers).
 *
 * @author Joao Goncalves: jcfgonc@gmail.com
 *
 * @param <T>
 */
public class ObjectIndex<T> {

	Int2ObjectOpenHashMap<T> idToObject;
	Object2IntOpenHashMap<T> objectToId;
	IntOpenHashSet freedIDs;

	public ObjectIndex() {
		idToObject = new Int2ObjectOpenHashMap<>();
		objectToId = new Object2IntOpenHashMap<>();
		freedIDs = new IntOpenHashSet();
	}

	public ObjectIndex(int size) {
		idToObject = new Int2ObjectOpenHashMap<>(size);
		objectToId = new Object2IntOpenHashMap<>(size);
		freedIDs = new IntOpenHashSet();
	}

	public int addObject(T object) {
		int eid = objectToId.size();
		if (objectToId.containsKey(object)) {
			eid = objectToId.getInt(object);
		} else {
			// recycle existing Ids
			if (!freedIDs.isEmpty()) {
				eid = freedIDs.iterator().next(); // afaik no (external to the class) way of optimizing this
				freedIDs.remove(eid);
			}
			objectToId.put(object, eid);
			idToObject.put(eid, object);
		}
		return eid;
	}

	/**
	 * overwrites object/id
	 * 
	 * @param object
	 * @param eid
	 */
	public void addObject(T object, int eid) {
		objectToId.put(object, eid);
		idToObject.put(eid, object);
	}

	public int removeObject(T object) {
		int id = objectToId.removeInt(object);
		freedIDs.add(id);
		idToObject.remove(id);
		return id;
	}

	public void clear() {
		objectToId.clear();
		idToObject.clear();
	}

	public T getObject(int id) {
		return idToObject.get(id);
	}

	public int getObjectId(T object) {
		return objectToId.getInt(object);
	}

	@Override
	public String toString() {
		return idToObject.toString();
	}

	public boolean containsObject(T object) {
		return objectToId.containsKey(object);
	}

	public boolean containsID(int id) {
		return idToObject.containsKey(id);
	}

	public void addObjects(Collection<T> objects) {
		for (T object : objects) {
			addObject(object);
		}
	}

}
