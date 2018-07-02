package autofe.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import autofe.algorithm.hasco.filter.generic.AddConstantFilter;
import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import autofe.algorithm.hasco.filter.image.LocalBinaryPatternFilter;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.algorithm.hasco.filter.meta.UnionFilter;
import hasco.model.Component;

public final class FilterUtils {
	private FilterUtils() {
		// Utility class
	}

	public static IFilter getFilterForName(final String name, final Map<String, String> parameters) {
		// Image filters
		if (name.startsWith("autofe.algorithm.hasco.filter.image")) {
			switch (name) {
			case "autofe.algorithm.hasco.filter.image.CatalanoWrapperFilter":
				String paramValue = parameters.get("catFilter");
				return ImageUtils.getCatalanoFilterByName(paramValue);
			case "autofe.algorithm.hasco.filter.image.LocalBinaryPatternFilter":
				return new LocalBinaryPatternFilter();
			case "NoneExtractor":
				return getDefaultFilter();
			}
		}

		switch (name) {
		case "autofe.algorithm.hasco.filter.generic.AddConstantFilter":
			return new AddConstantFilter();
		case "autofe.algorithm.hasco.filter.generic.IdentityFilter":
			return new IdentityFilter();
		case "autofe.MakeUnion":
			return new UnionFilter();
		default:
			return getDefaultFilter();
		}
	}

	public static IFilter getDefaultFilter() {
		return new IdentityFilter();
	}

	public static List<Component> getDefaultComponents() {
		final List<Component> components = new ArrayList<>();

		Component c = new Component("autofe.algorithm.hasco.filter.generic.AddConstantFilter");
		c.addProvidedInterface("filter");
		components.add(c);

		Component c1 = new Component("autofe.algorithm.hasco.filter.generic.IdentityFilter");
		c1.addProvidedInterface("filter");
		components.add(c1);

		Component c2 = new Component("FilterPipeline");
		c2.addRequiredInterface("filter", "filter");
		components.add(c2);

		return components;
	}
}
