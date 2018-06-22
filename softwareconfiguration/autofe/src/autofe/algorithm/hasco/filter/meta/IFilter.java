package autofe.algorithm.hasco.filter.meta;

import autofe.util.DataSet;

public interface IFilter<T> {
	public DataSet<T> applyFilter(final DataSet<T> inputData, final boolean copy);
}
