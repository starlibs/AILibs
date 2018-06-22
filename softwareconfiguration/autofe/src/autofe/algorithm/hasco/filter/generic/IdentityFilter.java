package autofe.algorithm.hasco.filter.generic;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

public class IdentityFilter<T> implements IFilter<T> {

	@Override
	public DataSet<T> applyFilter(DataSet<T> inputData, final boolean copy) {
		return inputData;
	}

}
