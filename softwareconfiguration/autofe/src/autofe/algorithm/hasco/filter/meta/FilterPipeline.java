package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jaicore.graph.Graph;
import jaicore.graph.Tree;

// TODO: Integrate descriptive statistics
@SuppressWarnings("serial")
public class FilterPipeline implements IFilter, Serializable {
//	private List<IFilter> filters = new ArrayList<>();
	private Graph<IFilter> filters;

	public FilterPipeline(final Graph<IFilter> filters) {
		this.filters = filters;
	}

	@Override
	public Collection<?> applyFilter(final Collection<?> inputData) {
		Collection<?> actInstances = inputData;
		
		// TODO: Implement filter application (traversing through graph)
		
//		for (IFilter filter : this.filters) {
//			actInstances = filter.applyFilter(actInstances);
//		}
		
		return actInstances;
	}

	// TODO: Use StringBuilder
	@Override
	public String toString() {
		String filterNames = "FilterPipeline: ";
		for (IFilter filter : this.filters.getItems())
			filterNames += filter.getClass().getSimpleName() + ", ";
		return filterNames;
	}
}
