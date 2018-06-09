package autofe.algorithm.hasco.filter.meta;

import java.util.Collection;

public interface IFilter {
	public Collection<?> applyFilter(final Collection<?> inputData);
}
