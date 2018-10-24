package com.githhub.aaronbembenek.querykb;

import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;

public final class Util {

	private Util() {
		throw new AssertionError();
	}
	
	public static <K, V> V lookupOrCreate(Map<K, V> m, K k, Supplier<V> vthunk) {
		V v2 = m.get(k);
		if (v2 == null) {
			V v = vthunk.get();
			v2 = m.putIfAbsent(k, v);
			if (v2 == null) {
				v2 = v;
			}
		}
		return v2;
	}
	
	public static <K> int lookupOrCreate(TObjectIntMap<K> m, K k, IntSupplier vthunk) {
		if (!m.containsKey(k)) {
			m.putIfAbsent(k, vthunk.getAsInt());
		}
		return m.get(k);
	}
	
	public static <V> V lookupOrCreate(TIntObjectMap<V> m, int k, Supplier<V> vthunk) {
		if (!m.containsKey(k)) {
			m.putIfAbsent(k, vthunk.get());
		}
		return m.get(k);
	}
	
	public static int lookupOrCreate(TIntIntMap m, int k, IntSupplier vthunk) {
		if (!m.containsKey(k)) {
			m.putIfAbsent(k, vthunk.getAsInt());
		}
		return m.get(k);
	}
	
	public static class IntPair {
		
		public int fst;
		public int snd;
		
	}
	
}
