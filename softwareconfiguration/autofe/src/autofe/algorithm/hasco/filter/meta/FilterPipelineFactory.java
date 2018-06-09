package autofe.algorithm.hasco.filter.meta;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.util.FilterUtils;
import hasco.model.ComponentInstance;
import hasco.query.Factory;

public class FilterPipelineFactory implements Factory<FilterPipeline> {

	private static final Logger logger = LoggerFactory.getLogger(FilterPipelineFactory.class);

	@Override
	public FilterPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {

		ComponentInstance filterCI = null;

		switch (groundComponent.getComponent().getName()) {
		case "pipeline":
			filterCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("filter");
			break;

		default:
			filterCI = groundComponent;
			break;
		}

		final List<IFilter> filters = new ArrayList<>();

		// TODO: Parameter list
		for (ComponentInstance actFilterCI : filterCI.getSatisfactionOfRequiredInterfaces().values()) {
			IFilter tmpFilter = FilterUtils.getFilterForName(actFilterCI.getComponent().getName());
			if (tmpFilter != null)
				filters.add(tmpFilter);
			else
				logger.warn(
						"Could not retrieve filter named '" + actFilterCI.getComponent().getName() + "'. Skipping...");

		}

		return new FilterPipeline(filters);
	}

}
