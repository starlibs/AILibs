package autofe.util;

import autofe.algorithm.hasco.filter.generic.AddConstantFilter;
import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import autofe.algorithm.hasco.filter.meta.ForwardFilter;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.algorithm.hasco.filter.meta.UnionFilter;

public final class FilterUtils {
	private FilterUtils() {
		// Utility class
	}

	@SuppressWarnings("unchecked")
	public static <T> IFilter<T> getFilterForName(final String name) {
		// TODO: Make filter selection automatically (instantiate class by fully
		// classified class name)
		switch (name) {
		case "autofe.algorithm.hasco.filter.generic.AddConstantFilter":
			return (IFilter<T>) new AddConstantFilter();
		case "autofe.algorithm.hasco.filter.generic.IdentityFilter":
			return new IdentityFilter<T>();
		case "autofe.MakeUnion":
			return new UnionFilter<T>();
		case "autofe.MakeForward":
			return new ForwardFilter<T>();
		default:
			return null;
		}
	}
}
