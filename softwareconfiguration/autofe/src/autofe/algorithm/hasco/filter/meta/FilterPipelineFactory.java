package autofe.algorithm.hasco.filter.meta;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.util.FilterUtils;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.BaseFactory;
import jaicore.graph.Graph;

public class FilterPipelineFactory implements BaseFactory<FilterPipeline> {

	private static final String UNION_NAME = "autofe.MakeUnion";
	private static final String ABSTRACT_PIPE_NAME = "AbstractPipe";
	private static final String NNPIPE_NAME = "NNPipe";
	private static final String PREP_PIPE_NAME = "PrepPipe";

	private static final Logger logger = LoggerFactory.getLogger(FilterPipelineFactory.class);

	private final long[] inputShape;

	public FilterPipelineFactory(final long[] inputShape) {
		this.inputShape = inputShape;
	}

	@Override
	public FilterPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {
		if (groundComponent == null || groundComponent.getComponent() == null) {
			return null;
		}

		Graph<IFilter> filterGraph = new Graph<>();
		Queue<ComponentInstance> open = new LinkedList<>();
		Queue<IFilter> openFilter = new LinkedList<>();

		switch (groundComponent.getComponent().getName()) {
		case "FilterPipeline":
			ComponentInstance actCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("pipe");
			// ComponentInstance initCI =
			// groundComponent.getSatisfactionOfRequiredInterfaces().get("pipe");
			if (actCI == null) {
				return new FilterPipeline(null);
			}

			IFilter actCIFilter = FilterUtils.getFilterForName(actCI.getComponent().getName(),
					actCI.getParameterValues(), this.inputShape);
			filterGraph.addItem(actCIFilter);
			openFilter.offer(actCIFilter);
			open.offer(actCI);

			logger.debug("Building pipeline: " + FilterUtils.getPrettyPrint(actCI, 0));

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
					if (filter1CI != null) {
						IFilter filter1 = FilterUtils.getFilterForName(filter1CI.getComponent().getName(),
								filter1CI.getParameterValues(), this.inputShape);

						open.offer(filter1CI);
						openFilter.offer(filter1);

						// Update graph
						filterGraph.addItem(filter1);
						filterGraph.addEdge(actCIFilter, filter1);
					}

					ComponentInstance filter2CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter2");
					if (filter2CI != null) {
						IFilter filter2 = FilterUtils.getFilterForName(filter2CI.getComponent().getName(),
								filter2CI.getParameterValues(), this.inputShape);

						open.offer(filter2CI);
						openFilter.offer(filter2);

						// Update graph
						filterGraph.addItem(filter2);
						filterGraph.addEdge(actCIFilter, filter2);
					}

					break;
				case PREP_PIPE_NAME:
					ComponentInstance prepCI = actCI.getSatisfactionOfRequiredInterfaces().get("preprocessor");
					IFilter prep = null;
					if (prepCI != null) {
						prep = FilterUtils.getFilterForName(prepCI.getComponent().getName(),
								prepCI.getParameterValues(), this.inputShape);
						filterGraph.addItem(prep);
						filterGraph.addEdge(actCIFilter, prep);
						actCIFilter = prep;
					} else {
						break;
					}

					ComponentInstance furtherCI = actCI.getSatisfactionOfRequiredInterfaces().get("further");
					if (furtherCI != null) {
						IFilter furtherFilter = FilterUtils.getFilterForName(furtherCI.getComponent().getName(),
								furtherCI.getParameterValues(), this.inputShape);

						open.offer(furtherCI);
						openFilter.offer(furtherFilter);

						filterGraph.addItem(furtherFilter);
						filterGraph.addEdge(actCIFilter, furtherFilter);
					}

					break;
				case ABSTRACT_PIPE_NAME:
				case NNPIPE_NAME:
					// Extractor
					ComponentInstance extractorCI = null;
					IFilter extractor = null;
					if (actCI.getComponent().getName().equals(ABSTRACT_PIPE_NAME)) {
						extractorCI = actCI.getSatisfactionOfRequiredInterfaces().get("extractor");
						if (extractorCI == null) {
							break;
						}

						extractor = FilterUtils.getFilterForName(extractorCI.getComponent().getName(),
								extractorCI.getParameterValues(), this.inputShape);
						filterGraph.addItem(extractor);
						filterGraph.addEdge(actCIFilter, extractor);

						actCIFilter = extractor;
					} else {
						extractorCI = actCI.getSatisfactionOfRequiredInterfaces().get("net");
						if (extractorCI == null) {
							break;
						}

						extractor = FilterUtils.getFilterForName(extractorCI.getComponent().getName(),
								extractorCI.getParameterValues(), this.inputShape);

						// If pretrained neural net can not be applied to given input shape (e. g. due
						// to different channel amount) return empty filter pipeline (checked in node
						// evaluators)
						if (((PretrainedNNFilter) extractor).getCompGraph() == null) {
							return new FilterPipeline(null);
						}

						filterGraph.addItem(extractor);
						filterGraph.addEdge(actCIFilter, extractor);

						actCIFilter = extractor;
					}

					// Preprocessors
					// Deepening pipe
					ComponentInstance preprocessorCI = actCI.getSatisfactionOfRequiredInterfaces().get("preprocessors");
					if (preprocessorCI == null) {
						break;
					}

					IFilter preprocessor = null;
					if (!preprocessorCI.getComponent().getName().equals("PrepPipe")) {
						// Just one basic filter
						preprocessor = FilterUtils.getFilterForName(preprocessorCI.getComponent().getName(),
								preprocessorCI.getParameterValues(), this.inputShape);
						filterGraph.addItem(preprocessor);
						filterGraph.addEdge(actCIFilter, preprocessor);
					} else {
						// Preprocessor pipeline
						IFilter newActCIFilter = actCIFilter;
						while (preprocessorCI != null && preprocessorCI.getComponent().getName().equals("PrepPipe")) {
							ComponentInstance childCI = preprocessorCI.getSatisfactionOfRequiredInterfaces()
									.get("preprocessor");

							if (childCI == null) {
								break;
							}

							IFilter childFilter = FilterUtils.getFilterForName(childCI.getComponent().getName(),
									childCI.getParameterValues(), this.inputShape);
							filterGraph.addItem(childFilter);
							filterGraph.addEdge(newActCIFilter, childFilter);

							newActCIFilter = childFilter;
							preprocessorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("further");
						}

						// End of pipeline reached
						if (preprocessorCI != null) {
							preprocessor = FilterUtils.getFilterForName(preprocessorCI.getComponent().getName(),
									preprocessorCI.getParameterValues(), this.inputShape);
							filterGraph.addItem(preprocessor);
							filterGraph.addEdge(newActCIFilter, preprocessor);
						}
					}

				default:
					// Basic filter
					break;
				}
			}
		}

		FilterPipeline result = new FilterPipeline(filterGraph);

		logger.debug("Result pipeline after build: " + result.toString());
		return result;

	}
}
