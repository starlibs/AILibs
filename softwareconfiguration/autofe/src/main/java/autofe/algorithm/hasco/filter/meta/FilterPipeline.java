package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.graph.Graph;
import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.util.DataSet;

@SuppressWarnings("serial")
public class FilterPipeline implements IFilter, Serializable, EvaluatedSoftwareConfigurationSolution<Double> {

	private static final Logger logger = LoggerFactory.getLogger(FilterPipeline.class);

	private final transient ComponentInstance ci;
	private final transient Graph<IFilter> filters;

	class FilterDataEntry implements Comparable<FilterDataEntry> {
		private IFilter filter;
		private DataSet dataset;

		public FilterDataEntry(final IFilter filter, final DataSet dataset) {
			this.filter = filter;
			this.dataset = dataset;
		}

		@Override
		public int compareTo(final FilterDataEntry arg0) {
			return this.filter.getClass().getName().compareTo(arg0.getClass().getName());
		}

		@Override
		public boolean equals(final Object o) {
			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public FilterPipeline(final ComponentInstance ci, final Graph<IFilter> filters) {
		this.ci = ci;
		this.filters = filters;
	}

	@Override
	public DataSet applyFilter(final DataSet data, final boolean copy) throws InterruptedException {
		if (this.filters == null) {
			return data;
		}

		// Copy graph
		Graph<FilterDataEntry> dataGraph = this.copyGraphIntoDataGraph();

		// Create a new data copy for each leaf node
		List<FilterDataEntry> leafNodes = new ArrayList<>(dataGraph.getSinks());
		HashSet<FilterDataEntry> nextNodes = new HashSet<>();
		for (FilterDataEntry entry : leafNodes) {
			entry.dataset = entry.filter.applyFilter(data, true);
			nextNodes.addAll(dataGraph.getPredecessors(entry));
		}

		// Iterate through graph to generate data sets by applying filters
		while (!nextNodes.isEmpty()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Execution of filter pipeline got interrupted.");
			}
			FilterDataEntry nextEntry = pollRandomElementFromSet(nextNodes);

			if (nextEntry == null) {
				throw new IllegalStateException("Could not poll first entry from tree set which is not empty.");
			}

			// Detect cycles
			if (nextEntry.dataset != null) {
				logger.warn("Detected cycle in the filter graph.");
			} else {
				boolean addPredecessors = true;

				// Check for union
				Set<FilterDataEntry> successors = dataGraph.getSuccessors(nextEntry);
				if (successors.isEmpty()) {
					throw new IllegalStateException("Entry propagated to the working set by a successor should have successors.");
				}
				if (successors.size() > 1) {
					// Union
					Iterator<FilterDataEntry> it = successors.iterator();
					FilterDataEntry succ1 = it.next();
					FilterDataEntry succ2 = it.next();

					if (succ1.dataset == null || succ2.dataset == null) {
						nextNodes.add(nextEntry);
						continue;
					}

					nextEntry.dataset = nextEntry.filter.applyFilter(UnionFilter.union(succ1.dataset, succ2.dataset), false);
					dataGraph = this.eraseSubTreeData(dataGraph, nextEntry);

				} else {
					FilterDataEntry successor = successors.iterator().next();
					if (successor.dataset != null) {
						nextEntry.dataset = nextEntry.filter.applyFilter(successor.dataset, false);
					} else {
						nextNodes.add(nextEntry);
						addPredecessors = false;
					}
				}
				if (addPredecessors) {
					// Add predecessors to working set
					nextNodes.addAll(dataGraph.getPredecessors(nextEntry));
				}
			}
		}

		DataSet resultDataSet = data;
		if (!dataGraph.getItems().isEmpty()) {
			resultDataSet = dataGraph.getRoot().dataset;
		}

		// Update intermediate instances into Weka instances
		logger.debug("Updating instances...");
		resultDataSet.updateInstances();
		logger.debug("Done.");
		return resultDataSet;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("FilterPipeline: ");
		if (this.filters == null) {
			result.append("Empty");
		} else {
			boolean first = true;
			for (IFilter filter : this.filters.getItems()) {
				if (first) {
					first = false;
				} else {
					result.append(", ");
				}
				result.append(filter.toString());
			}
		}
		return result.toString();
	}

	// Only used to remove temporary stored data instances (called when using union)
	private Graph<FilterDataEntry> eraseSubTreeData(final Graph<FilterDataEntry> graph, final FilterDataEntry parent) {
		TreeSet<FilterDataEntry> nextSuccs = new TreeSet<>(graph.getSuccessors(parent));

		// Relies on tree structure of the graph
		while (!nextSuccs.isEmpty()) {
			FilterDataEntry succ = nextSuccs.pollFirst();
			if (succ != null && succ.dataset != null) {
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

	private static FilterDataEntry pollRandomElementFromSet(final HashSet<FilterDataEntry> set) {
		int size = set.size();
		FilterDataEntry selectedElem = null;
		if (size > 1) {
			int itemIndex = new Random().nextInt(size);
			int i = 0;
			for (FilterDataEntry entry : set) {
				if (i == itemIndex) {
					selectedElem = entry;
					break;
				}
				i++;
			}
		}
		if (selectedElem == null) {
			selectedElem = set.iterator().next();
		}
		set.remove(selectedElem);
		return selectedElem;
	}

	public Graph<IFilter> getFilters() {
		return this.filters;
	}

	public boolean containsPretrainedNN() {
		for (IFilter filter : this.getFilters().getItems()) {
			if (filter instanceof PretrainedNNFilter) {
				return true;
			}
		}

		return false;
	}

	@Override
	public FilterPipeline clone() throws CloneNotSupportedException {
		super.clone();
		// Filter mapping for reconstruction
		Map<IFilter, IFilter> filterMapping = new HashMap<>();

		for (IFilter filter : this.filters.getItems()) {
			filterMapping.put(filter, (IFilter) filter.clone());
		}

		Graph<IFilter> clonedGraph = new Graph<>();
		for (Map.Entry<IFilter, IFilter> entry : filterMapping.entrySet()) {
			clonedGraph.addItem(entry.getValue());
		}
		for (Pair<IFilter, IFilter> pair : this.filters.getEdges()) {
			clonedGraph.addEdge(filterMapping.get(pair.getX()), filterMapping.get(pair.getY()));
		}

		return new FilterPipeline(this.ci, clonedGraph);
	}

	@Override
	public Double getScore() {
		return null;
	}

	@Override
	public ComponentInstance getComponentInstance() {
		return this.ci;
	}
}
