package bwsal_bridge.priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwsal_bridge.manager.BuildOrderManager.BuildItem;

public class PriorityMap implements Map<Integer, List<BuildItem>> {
	
	private Map<Integer, List<BuildItem>> items = new HashMap<Integer, List<BuildItem>>();
	private ArrayList<Integer> priorities = new ArrayList<>();
	
	public PriorityMap() {}
	
	public PriorityMap(Map<Integer, List<BuildItem>> items) {
		for (Entry<Integer, List<BuildItem>> item : items.entrySet()) {
			this.priorities.add(item.getKey());
		}
		Collections.sort(this.priorities);
		this.items = items;
	}
	
	public List<BuildItem> removeLast() {
		return remove(this.priorities.get(this.priorities.size() - 1));
	}
	
	public PriorityEntry getEntry(int i) {
		Integer priority = this.priorities.get(i);
		List<BuildItem> list = this.items.get(priority);
		return new PriorityEntry(priority, list);
	}
	
	public ArrayList<PriorityEntry> getAllEntries() {
		ArrayList<PriorityEntry> entries = new ArrayList<>();
		for (Integer priority : this.priorities) {
			
		}
	}
	
	public PriorityEntry getLastEntry() {
		Integer priority = this.priorities.get(this.priorities.size() - 1);
		List<BuildItem> list = this.items.get(priority);
		return new PriorityEntry(priority, list);
	}
	
	public class PriorityEntry implements Entry<Integer, List<BuildItem>> {
		
		private Integer key;
		private List<BuildItem> value;
		
		@Override
		public Integer getKey() {
			return this.key;
		}
	
		@Override
		public List<BuildItem> getValue() {
			return this.value;
		}
	
		@Override
		public List<BuildItem> setValue(List<BuildItem> value) {
			final List<BuildItem> oldValue = this.value;
			this.value = value;
			return oldValue;
		}
		
		public Integer setKey(Integer key) {
			final Integer oldKey = this.key;
			this.key = key;
			return oldKey;
		}
		
		public PriorityEntry(Integer key, List<BuildItem> value) {
			this.key = key;
			this.value = value;
		}
		
	}

	@Override
	public void clear() {
		this.priorities = new ArrayList<>();
		this.items = new HashMap<>();
	}

	@Override
	public boolean containsKey(Object key) {
		return (this.priorities.indexOf(key) > -1 && this.items.containsKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return (this.items.containsValue(value));
	}

	@Override
	public Set<java.util.Map.Entry<Integer, List<BuildItem>>> entrySet() {
		return this.items.entrySet();
	}

	@Override
	public List<BuildItem> get(Object key) {
		return this.items.get(key);
	}
	
	public List<BuildItem> getLast() {
		return this.items.get(this.priorities.get(this.priorities.size() - 1));
	}

	@Override
	public boolean isEmpty() {
		return (this.priorities.isEmpty() && this.items.isEmpty());
	}

	@Override
	public Set<Integer> keySet() {
		return this.items.keySet();
	}

	@Override
	public List<BuildItem> put(Integer key, List<BuildItem> value) {
		this.priorities.add(key);
		this.items.put(key, value);
		Collections.sort(this.priorities);
		return value;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends List<BuildItem>> m) {
		for (Entry<? extends Integer, ? extends List<BuildItem>> item : m.entrySet()) {
			put(item.getKey(), item.getValue());
		}
	}

	@Override
	public List<BuildItem> remove(Object key) {
		this.priorities.remove(this.priorities.indexOf(key));
		return this.items.remove(key);
	}

	@Override
	public int size() {
		if (this.priorities.size() == this.items.size()) return this.priorities.size();
		return 0;
	}

	@Override
	public Collection<List<BuildItem>> values() {
		return this.items.values();
	}

}
