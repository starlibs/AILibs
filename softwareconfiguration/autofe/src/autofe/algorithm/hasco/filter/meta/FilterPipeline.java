package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.util.DataSet;
import jaicore.graph.Graph;

// TODO: Integrate descriptive statistics
@SuppressWarnings("serial")
public class FilterPipeline implements IFilter, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(FilterPipeline.class);

	private Graph<IFilter> filters;

	class FilterDataEntry implements Comparable<FilterDataEntry> {
		IFilter filter;
		DataSet dataset;

		public FilterDataEntry(IFilter filter, DataSet dataset) {
			this.filter = filter;
			this.dataset = dataset;
		}

		@Override
		public int compareTo(FilterDataEntry arg0) {
			return filter.getClass().getName().compareTo(arg0.getClass().getName());
		}
	}

	public FilterPipeline(final Graph<IFilter> filters) {
		this.filters = filters;
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {
		// Copy graph
		Graph<FilterDataEntry> dataGraph = this.copyGraphIntoDataGraph();

		// Create a new data copy for each leaf node
		List<FilterDataEntry> leafNodes = new ArrayList<>(dataGraph.getSinks());
		TreeSet<FilterDataEntry> nextNodes = new TreeSet<>();
		for (FilterDataEntry entry : leafNodes) {
			entry.dataset = entry.filter.applyFilter(inputData, true);
			nextNodes.addAll(dataGraph.getPredecessors(entry));
		}
		// Iterate through graph to generate data sets by applying filters
		while (!nextNodes.isEmpty()) {
			FilterDataEntry nextEntry = nextNodes.pollFirst();
			if (nextEntry == null)
				throw new IllegalStateException("Could not poll first entry from tree set which is not empty.");

			// Detect cycles
			if (nextEntry.dataset != null) {
				logger.warn("Detected cycle in the filter graph.");
				continue;
			}

//			if (nextEntry.filter instanceof IAbstractFilter) {
//				logger.warn("Filter:" + nextEntry.filter.getClass().getName());
//				throw new IllegalStateException("Got an abstract filter which should not be stored in filter graph.");
//			}
			

			// Check for union
			Set<FilterDataEntry> successors = dataGraph.getSuccessors(nextEntry);
			if (successors.size() == 0) {
				throw new IllegalStateException(
						"Entry propagated to the working set by a successor should have successors.");
			}
			if (successors.size() > 1) {
				// Union
				Iterator<FilterDataEntry> it = successors.iterator();
				FilterDataEntry succ1 = it.next();
				FilterDataEntry succ2 = it.next();
				nextEntry.dataset = nextEntry.filter
						.applyFilter(new UnionFilter().union(succ1.dataset, succ2.dataset), true);
				dataGraph = this.eraseSubTreeData(dataGraph, nextEntry);
			} else {
				// TODO: Check this
				// Regular filter
//				if(!(nextEntry.filter instanceof ForwardFilter))
					nextEntry.dataset = nextEntry.filter.applyFilter(successors.iterator().next().dataset, false);
//				else
//					nextEntry.dataset = successors.iterator().next().dataset;
			}

			// Add predecessors to working set
			nextNodes.addAll(dataGraph.getPredecessors(nextEntry));
		}
		DataSet resultDataSet = dataGraph.getRoot().dataset;
		
		// Update intermediate instances into Weka instances
		resultDataSet.updateInstances();
		return resultDataSet;
	}

	// TODO: Use StringBuilder
	@Override
	public String toString() {
		String filterNames = "FilterPipeline: ";
		for (IFilter filter : this.filters.getItems())
			filterNames += filter.getClass().getSimpleName() + ", ";
		return filterNames;
	}

	// Only used to remove temporary stored data instances (called when using union)
	private Graph<FilterDataEntry> eraseSubTreeData(final Graph<FilterDataEntry> graph, final FilterDataEntry parent) {
		TreeSet<FilterDataEntry> nextSuccs = new TreeSet<>();
		nextSuccs.addAll(graph.getSuccessors(parent));

		// Relies on tree structure of the graph
		while (!nextSuccs.isEmpty()) {
			FilterDataEntry succ = nextSuccs.pollFirst();
			if (succ.dataset != null) {
				succ.dataset = null;
				nextSuccs.addAll(graph.getSuccessors(succ));
			}
		}
		return graph;
	}

	private Graph<FilterDataEntry> copyGraphIntoDataGraph() {
		// Copy graph
		Graph<FilterDataEntry> dataGraph = new Graph<>();
		Map<IFilter, FilterDataEntry> filterFDEMapping = new HashMap<>();
		// Add all items
		for (IFilter filter : this.filters.getItems()) {
			FilterDataEntry newEntry = new FilterDataEntry(filter, null);
			filterFDEMapping.put(filter, newEntry);
			dataGraph.addItem(newEntry);
		}
		// Add all edges
		for (IFilter filter : this.filters.getItems()) {
			for (IFilter succ : this.filters.getSuccessors(filter)) {
				dataGraph.addEdge(filterFDEMapping.get(filter), filterFDEMapping.get(succ));
			}
		}
		return dataGraph;
	}
}
