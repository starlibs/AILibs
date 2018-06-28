package autofe.algorithm.hasco.filter.generic;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

public class IdentityFilter implements IFilter {

	@Override
	public DataSet applyFilter(DataSet inputData, final boolean copy) {
		return inputData;
	}

}
