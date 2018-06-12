package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: Integrate descriptive statistics
@SuppressWarnings("serial")
public class FilterPipeline implements IFilter, Serializable {
	private List<IFilter> filters = new ArrayList<>();

	public FilterPipeline(final List<IFilter> filters) {
		this.filters.addAll(filters);
	}

	@Override
	public Collection<?> applyFilter(final Collection<?> inputData) {
		Collection<?> actInstances = inputData;
		for (IFilter filter : this.filters) {
			actInstances = filter.applyFilter(actInstances);
		}
		return actInstances;
	}

	// TODO: Use StringBuilder
	@Override
	public String toString() {
		String filterNames = "FilterPipeline: ";
		for (IFilter filter : this.filters)
			filterNames += filter.getClass().getSimpleName() + ", ";
		return filterNames;
	}
}
