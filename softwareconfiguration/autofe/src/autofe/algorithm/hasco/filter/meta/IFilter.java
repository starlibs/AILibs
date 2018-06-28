package autofe.algorithm.hasco.filter.meta;

import autofe.util.DataSet;

public interface IFilter {
	public DataSet applyFilter(final DataSet inputData, final boolean copy);
}
