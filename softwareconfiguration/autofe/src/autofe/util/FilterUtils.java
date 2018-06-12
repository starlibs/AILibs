package autofe.util;

import autofe.algorithm.hasco.filter.generic.AddConstantFilter;
import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import autofe.algorithm.hasco.filter.meta.IFilter;

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
		default:
			return null;
		}
	}
}
