package bwsal_bridge.priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import bwsal_bridge.manager.BuildOrderManager.BuildItem;

public class PriorityList implements List<Priority> {
//	TODO: Create Lookup System
	
	private ArrayList<Priority> items = new ArrayList<>();
	private HashMap<Integer, Integer> lookup = new HashMap<>();
	
	protected class PriorityComparator implements Comparator<Priority> {
		@Override
		public int compare(Priority arg0, Priority arg1) {
			return arg0.getPriority() - arg1.getPriority();
		}
	}
	
	public void sort() {
		Collections.sort(this.items, new PriorityComparator());
	}

	
	@Override
	public boolean add(Priority arg0) {
		if (this.items.add(arg0)) {
			this.sort();
			return true;
		}
		return false;
	}
	
	public void add(Integer priority, List<BuildItem> items) {
		this.items.add(new Priority(priority, items));
	}
	

	@Override
	public void add(int arg0, Priority arg1) {
		this.items.add(arg0, arg1);
		this.sort();
	}
	
	public void add(int i, Integer priority, List<BuildItem> items) {
		this.items.add(i, new Priority(priority, items));
	}
	

	@Override
	public boolean addAll(Collection<? extends Priority> arg0) {
		if (this.items.addAll(arg0)) {
			this.sort();
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends Priority> arg1) {
		if (this.items.addAll(arg0, arg1)) {
			this.sort();
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		this.items.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return this.items.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return this.items.containsAll(arg0);
	}

	@Override
	public Priority get(int arg0) {
		return this.items.get(arg0);
	}
	
	public Priority last() {
		this.sort();
		return this.items.get(this.items.size() - 1);
	}
	

	@Override
	public int indexOf(Object arg0) {
		return this.items.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	@Override
	public Iterator<Priority> iterator() {
		return this.items.iterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		return this.items.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<Priority> listIterator() {
		return this.items.listIterator();
	}

	@Override
	public ListIterator<Priority> listIterator(int arg0) {
		return this.items.listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		if (this.items.remove(arg0)) {
			this.sort();
			return true;
		}
		return false;
	}

	@Override
	public Priority remove(int arg0) {
		Priority priority = this.items.remove(arg0);
		this.sort();
		return priority;
	}
	
	public Priority removeLast() {
		this.sort();
		Priority priority = this.last();
		this.remove(this.size() - 1);
		this.sort();
		return priority;
	}
	

	@Override
	public boolean removeAll(Collection<?> arg0) {
		if (this.items.removeAll(arg0)) {
			this.sort();
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		if (this.items.retainAll(arg0)) {
			this.sort();
			return true;
		}
		return false;
	}

	@Override
	public Priority set(int arg0, Priority arg1) {
		Priority oldPriority = this.items.get(arg0);
		this.items.set(arg0, arg1);
		this.sort();
		return oldPriority;
	}

	@Override
	public int size() {
		return this.items.size();
	}

	@Override
	public List<Priority> subList(int arg0, int arg1) {
		PriorityList subList = (PriorityList) this.items.subList(arg0, arg1);
		subList.sort();
		return subList;
	}

	@Override
	public Object[] toArray() {
		return this.items.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.items.toArray(arg0);
	}
}
