package autofe.algorithm.hasco.filter.generic;

import java.util.Collection;

import autofe.algorithm.hasco.filter.meta.IFilter;

public class IdentityFilter implements IFilter {

	@Override
	public Collection<?> applyFilter(Collection<?> inputData) {
		return inputData;
	}

}
