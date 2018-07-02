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
	private static final String ABSTRACT_PIPE_NAME = "AbstractPipe";

	private static final Logger logger = LoggerFactory.getLogger(FilterPipelineFactory.class);

	@Override
	public FilterPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {

		Graph<IFilter> filterGraph = new Graph<>();
		Queue<ComponentInstance> open = new LinkedList<>();
		Queue<IFilter> openFilter = new LinkedList<>();

		switch (groundComponent.getComponent().getName()) {
		case "pipeline":

			ComponentInstance actCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("pipe");
			IFilter actCIFilter = FilterUtils.getFilterForName(actCI.getComponent().getName(),
					actCI.getParameterValues());
			filterGraph.addItem(actCIFilter);
			openFilter.offer(actCIFilter);
			open.offer(actCI);

			logger.debug("Building pipeline: " + actCI.getPrettyPrint());

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
					IFilter filter1 = FilterUtils.getFilterForName(filter1CI.getComponent().getName(),
							filter1CI.getParameterValues());
					ComponentInstance filter2CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter2");
					IFilter filter2 = FilterUtils.getFilterForName(filter2CI.getComponent().getName(),
							filter2CI.getParameterValues());
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
				case ABSTRACT_PIPE_NAME:
					// Extractor
					ComponentInstance extractorCI = actCI.getSatisfactionOfRequiredInterfaces().get("extractor");
					IFilter extractor = FilterUtils.getFilterForName(extractorCI.getComponent().getName(),
							extractorCI.getParameterValues());
					filterGraph.addItem(extractor);
					filterGraph.addEdge(actCIFilter, extractor);

					actCIFilter = extractor;

					// Preprocessors
					// Deepening pipe
					ComponentInstance preprocessorCI = actCI.getSatisfactionOfRequiredInterfaces().get("preprocessors");
					IFilter preprocessor = null;
					if (!preprocessorCI.getComponent().getName().equals("PrepPipe")) {
						// Just one basic filter
						preprocessor = FilterUtils.getFilterForName(preprocessorCI.getComponent().getName(),
								preprocessorCI.getParameterValues());
						filterGraph.addItem(preprocessor);
						filterGraph.addEdge(actCIFilter, preprocessor);
					} else {
						// Preprocessor pipeline
						IFilter newActCIFilter = actCIFilter;
						while (preprocessorCI.getComponent().getName().equals("PrepPipe")) {
							ComponentInstance childCI = preprocessorCI.getSatisfactionOfRequiredInterfaces()
									.get("preprocessor");
							IFilter childFilter = FilterUtils.getFilterForName(childCI.getComponent().getName(),
									childCI.getParameterValues());
							filterGraph.addItem(childFilter);
							filterGraph.addEdge(newActCIFilter, childFilter);

							newActCIFilter = childFilter;
							preprocessorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("further");
						}
						// End of pipeline reached
						preprocessor = FilterUtils.getFilterForName(preprocessorCI.getComponent().getName(),
								preprocessorCI.getParameterValues());
						filterGraph.addItem(preprocessor);
						filterGraph.addEdge(newActCIFilter, preprocessor);
					}

				default:
					// Basic filter
					break;
				}
			}
		}

		return new FilterPipeline(filterGraph);

	}
}
