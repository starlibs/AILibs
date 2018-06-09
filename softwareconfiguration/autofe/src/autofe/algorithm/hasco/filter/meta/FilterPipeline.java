package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;

// TODO: Integrate descriptive statistics
@SuppressWarnings("serial")
public class FilterPipeline implements IFilter, Serializable {
	private List<IFilter> filters = new ArrayList<>();

	public FilterPipeline(final List<IFilter> filters) {
		this.filters.addAll(filters);
	}

	@Override
	public Instances applyFilter(final Instances inputData) {
		Instances actInstances = inputData;
		for (IFilter filter : this.filters) {
			actInstances = filter.applyFilter(actInstances);
		}
		return actInstances;
	}
}
