package bwsal_bridge.priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PriorityList implements List<Integer> {
	
	private ArrayList<Integer> priorities;
	
	@Override
	public boolean add(Integer e) {
		if (this.priorities.add(e)) {
			Collections.sort(this.priorities);
			return true;
		}
		return false;
	}

	@Override
	public void add(int index, Integer element) {
		add(element);
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		if (this.priorities.addAll(c)) {
			Collections.sort(this.priorities);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Integer> c) {
		if (this.priorities.addAll(index, c)) {
			Collections.sort(this.priorities);
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		this.priorities.clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.priorities.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.priorities.containsAll(c);
	}

	@Override
	public Integer get(int index) {
		return this.priorities.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return this.priorities.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return this.priorities.isEmpty();
	}

	@Override
	public Iterator<Integer> iterator() {
		return this.priorities.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.priorities.lastIndexOf(o);
	}

	@Override
	public ListIterator<Integer> listIterator() {
		return this.priorities.listIterator();
	}

	@Override
	public ListIterator<Integer> listIterator(int index) {
		return this.priorities.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		if (this.priorities.remove(o)) {
			Collections.sort(this.priorities);
			return true;
		}
		return false;
	}

	@Override
	public Integer remove(int index) {
		Integer oldInt = this.priorities.remove(index);
		Collections.sort(this.priorities);
		return oldInt;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (this.priorities.removeAll(c)) {
			Collections.sort(this.priorities);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer set(int index, Integer element) {
		Integer set = this.priorities.set(index, element);
		Collections.sort(this.priorities);
		return set;
	}

	@Override
	public int size() {
		return this.priorities.size();
	}

	@Override
	public List<Integer> subList(int fromIndex, int toIndex) {
		return this.priorities.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return this.priorities.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.priorities.toArray(a);
	}

}
