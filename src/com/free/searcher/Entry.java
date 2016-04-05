package com.free.searcher;

import java.util.Map;

public class Entry<K extends Comparable<K>, V> implements
		Map.Entry<K, V>, java.io.Serializable, Comparable<Entry<K, V>> {

	private static final long serialVersionUID = 5887584761454864149L;
	private K key;
	private V value;

	public Entry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}
	public K setKey(K key) {
		K oldValue = this.key;
		this.key = key;
		return oldValue;
	}
	
	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Map.Entry))
			return false;
		Map.Entry e = (Map.Entry) o;
		return eq(key, e.getKey());
	}

	private static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public int hashCode() {
		return (key == null ? 0 : key.hashCode())
				^ (value == null ? 0 : value.hashCode());
	}

	public String toString() {
		return key + "=" + value;
	}

	@Override
	public int compareTo(Entry<K, V> o) {
		return this.getKey().compareTo(o.getKey());
	}

	
}
