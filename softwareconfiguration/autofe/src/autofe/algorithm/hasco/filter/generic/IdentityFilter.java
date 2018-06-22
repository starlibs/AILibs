package autofe.algorithm.hasco.filter.generic;

import java.util.Collection;

import autofe.algorithm.hasco.filter.meta.IFilter;

public class IdentityFilter<T> implements IFilter<T> {

	@Override
	public Collection<T> applyFilter(Collection<T> inputData, final boolean copy) {
		return inputData;
	}

}
