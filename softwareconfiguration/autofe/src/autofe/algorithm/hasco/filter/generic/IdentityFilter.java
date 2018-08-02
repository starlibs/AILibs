package autofe.algorithm.hasco.filter.generic;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

/**
 * Identity (none) filter.
 * 
 * @author Julian Lienen
 *
 */
public class IdentityFilter implements IFilter {

	@Override
	public DataSet applyFilter(DataSet inputData, final boolean copy) {
		if (copy)
			return inputData.copy();
		else
			return inputData;
	}

	@Override
	public String toString() {
		return "IdentityFilter";
	}
}
