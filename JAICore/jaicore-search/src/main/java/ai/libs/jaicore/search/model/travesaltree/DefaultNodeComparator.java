package ai.libs.jaicore.search.model.travesaltree;

import java.util.Comparator;

public class DefaultNodeComparator<N, A, V extends Comparable<V>> implements Comparator<BackPointerPath<N, A, V>> {

	@Override
	public int compare(final BackPointerPath<N, A, V> arg0, final BackPointerPath<N, A, V> arg1) {
		return arg0.getScore().compareTo(arg1.getScore());
	}
}
