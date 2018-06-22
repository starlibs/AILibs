package autofe.algorithm.hasco.filter.meta;

import java.util.Collection;

public class UnionFilter<T> implements IFilter<T>, IAbstractFilter {

	@Override
	public Collection<T> applyFilter(Collection<T> inputData, final boolean copy) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<T> union(final Collection<T> coll1, final Collection<T> coll2) {
		if (coll1.size() != coll2.size())
			throw new IllegalArgumentException(
					"Union operation requires two collections with same number of instances.");

		for (T obj : coll1) {
		}
		return null;
	}
}
