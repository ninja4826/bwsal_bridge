package bwsal_bridge.priority;

import java.util.Map.Entry;

public class PriorityEntry<K, V> implements Entry<K, V> {
	
	private K key;
	private V value;
	
	@Override
	public K getKey() {
		return this.key;
	}

	@Override
	public V getValue() {
		return this.value;
	}

	@Override
	public V setValue(V value) {
		final V oldValue = this.value;
		this.value = value;
		return oldValue;
	}
	
	public K setKey(K key) {
		final K oldKey = this.key;
		this.key = key;
		return oldKey;
	}
	
	public PriorityEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
}
