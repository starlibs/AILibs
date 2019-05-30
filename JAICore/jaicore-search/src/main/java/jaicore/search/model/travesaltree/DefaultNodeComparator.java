package jaicore.search.model.travesaltree;

import java.util.Comparator;

public class DefaultNodeComparator<N, V extends Comparable<V>> implements Comparator<Node<N,V>> {

	@Override
	public int compare(final Node<N, V> arg0, final Node<N, V> arg1) {
		return arg0.getInternalLabel().compareTo(arg1.getInternalLabel());
	}
}
