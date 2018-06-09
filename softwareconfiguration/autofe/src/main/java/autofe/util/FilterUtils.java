package autofe.util;

import autofe.algorithm.hasco.filter.meta.IFilter;

public final class FilterUtils {
	private FilterUtils() {
		// Utility class
	}

	public static IFilter getFilterForName(final String className) {
		// TODO: Make filter selection automatically
		switch (className) {
		case "test":
			return null;
		default:
			return null;
		}
	}
}
