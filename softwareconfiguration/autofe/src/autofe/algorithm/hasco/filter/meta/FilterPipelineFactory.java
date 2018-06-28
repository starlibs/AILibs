package autofe.algorithm.hasco.filter.meta;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.util.FilterUtils;
import hasco.model.ComponentInstance;
import hasco.query.Factory;
import jaicore.graph.Graph;

public class FilterPipelineFactory implements Factory<FilterPipeline> {

	private static final String UNION_NAME = "autofe.MakeUnion";
	private static final String FORWARD_NAME = "autofe.MakeForward";

	private static final Logger logger = LoggerFactory.getLogger(FilterPipelineFactory.class);

	@Override
	public FilterPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {

		Graph<IFilter> filterGraph = new Graph<>();
		Queue<ComponentInstance> open = new LinkedList<>();
		Queue<IFilter> openFilter = new LinkedList<>();

		switch (groundComponent.getComponent().getName()) {
		case "pipeline":

			ComponentInstance actCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("pipe");
			IFilter actCIFilter = FilterUtils.getFilterForName(actCI.getComponent().getName());
			filterGraph.addItem(actCIFilter);
			openFilter.offer(actCIFilter);
			open.offer(actCI);

			// Apply breadth-first-search
			while (!open.isEmpty()) {
				actCI = open.poll();
				actCIFilter = openFilter.poll();

				if (actCI == null) {
					logger.warn("Found null component. Breaking...");
					break;
				}

				switch (actCI.getComponent().getName()) {
				case UNION_NAME:
					ComponentInstance filter1CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter1");
					IFilter filter1 = FilterUtils.getFilterForName(filter1CI.getComponent().getName());
					ComponentInstance filter2CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter2");
					IFilter filter2 = FilterUtils.getFilterForName(filter2CI.getComponent().getName());
					open.offer(filter1CI);
					open.offer(filter2CI);
					openFilter.offer(filter1);
					openFilter.offer(filter2);

					// Update graph
					filterGraph.addItem(filter1);
					filterGraph.addItem(filter2);
					filterGraph.addEdge(actCIFilter, filter1);
					filterGraph.addEdge(actCIFilter, filter2);

					break;
				case FORWARD_NAME:
					ComponentInstance filterCI = actCI.getSatisfactionOfRequiredInterfaces().get("filter");
					IFilter filter = FilterUtils.getFilterForName(filterCI.getComponent().getName());
					ComponentInstance sourceCI = actCI.getSatisfactionOfRequiredInterfaces().get("source");
					IFilter source = FilterUtils.getFilterForName(sourceCI.getComponent().getName());
					open.offer(filterCI);
					open.offer(sourceCI);
					openFilter.offer(filter);
					openFilter.offer(source);

					// Update graph
					filterGraph.addItem(filter);
					filterGraph.addItem(source);
					filterGraph.addEdge(actCIFilter, filter);
					filterGraph.addEdge(actCIFilter, source);
					break;
				default:
					// Basic filter
					break;
				}
			}
		}

		// // TODO: Parameter list (filters need an interface so set them)
		// for (ComponentInstance actFilterCI :
		// filterCI.getSatisfactionOfRequiredInterfaces().values()) {
		// IFilter tmpFilter =
		// FilterUtils.getFilterForName(actFilterCI.getComponent().getName());
		// if (tmpFilter != null)
		// filters.add(tmpFilter);
		// else
		// logger.warn(
		// "Could not retrieve filter named '" + actFilterCI.getComponent().getName() +
		// "'. Skipping...");
		//
		// }

		return new FilterPipeline(filterGraph);

	}
}
