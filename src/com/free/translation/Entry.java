package com.free.translation;

public class Entry<K extends Comparable<?>, V> implements
		Comparable<Entry<K, V>> {
	private K key;
	private V value;

	public Entry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + key + "]: " + "[" + value + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Entry<?, ?>)) {
			return false;
		} else {
			Entry<?, ?> other = (Entry<?, ?>) obj;
			return key.toString().equalsIgnoreCase(other.getKey().toString());
		}
	}

	@Override
	public int hashCode() {
		return (key == null ? 0 : key.hashCode())
				^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public int compareTo(Entry<K, V> o) {
		return this.key.toString().toLowerCase()
			.compareTo(o.key.toString().toLowerCase());
	}
}
