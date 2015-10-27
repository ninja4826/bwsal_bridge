package bwsal_bridge;

import java.util.Set;

public interface Controller<T, U extends Comparable<U>> {
	void onOffer(Set<T> objects);
	void onRevoke(T object, U bid);
	String getName();
	void update();
}
