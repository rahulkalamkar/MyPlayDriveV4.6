package com.hungama.myplay.activity.data.audiocaching;

import java.util.HashMap;

/**
 * The goal of this special custom HashMap is to increase/decrease the reference
 * counter when item is put to the map or requested to be removed from the map.
 * Once the counter reaches zero value is actually removed.
 *
 * Warning: the default c'tor should be used only! Other c'tors use
 * internal&final implementation in the HashMap
 *
 * @param <K>
 * @param <V>
 */
public class RefCountHashMap<K, V> extends HashMap<K, V> {
	private HashMap<K, Integer> mapRefCounters = new HashMap<K, Integer>();

	public RefCountHashMap() {
	}

	@Override
	public V put(K key, V value) {
		V ret = super.put(key, value);

		Integer refCounter = mapRefCounters.get(key);
		if (refCounter != null)
			mapRefCounters.put(key, refCounter + 1);
		else
			mapRefCounters.put(key, 1);

		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		mapRefCounters.clear();
	}

	@Override
	public V remove(Object key) {
		if (key == null)
			return null; // hack

		Integer refCounter = mapRefCounters.get(key);
		if (refCounter == null)
			return super.remove(key);

		if (refCounter - 1 <= 0) {
			// removal is allowed
			mapRefCounters.remove(key);
			return super.remove(key);
		}

		// otherwise just decrease the counter w/o actual removal from the map
		mapRefCounters.put((K) key, refCounter - 1);
		return get(key);
	}

	// public static void main(String[] args) {
	// RefCountHashMap<Integer, String> map = new RefCountHashMap<Integer,
	// String>();
	// map.put(1, "One");
	// map.put(2, "Two");
	// map.put(1, "One again");
	// System.out.println(map.toString());
	//
	// map.remove(1);
	// System.out.println(map.toString());
	//
	// map.remove(1);
	// System.out.println(map.toString());
	// }
}
