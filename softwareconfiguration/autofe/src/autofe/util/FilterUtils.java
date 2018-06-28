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

	public static IFilter getFilterForName(final String name) {
		// TODO: Make filter selection automatically (instantiate class by fully
		// classified class name)
		switch (name) {
		case "autofe.algorithm.hasco.filter.generic.AddConstantFilter":
			return new AddConstantFilter();
		case "autofe.algorithm.hasco.filter.generic.IdentityFilter":
			return new IdentityFilter();
		case "autofe.MakeUnion":
			return new UnionFilter();
		case "autofe.MakeForward":
			return new ForwardFilter();
		default:
			return null;
		}
	}
}
