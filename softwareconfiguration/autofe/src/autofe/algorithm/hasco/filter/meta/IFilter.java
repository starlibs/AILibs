package autofe.algorithm.hasco.filter.meta;

import java.util.Collection;

public interface IFilter<T> {
	public Collection<T> applyFilter(final Collection<T> inputData, final boolean copy);
}
