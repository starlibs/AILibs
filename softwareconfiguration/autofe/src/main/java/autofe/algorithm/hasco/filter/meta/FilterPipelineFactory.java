package autofe.algorithm.hasco.filter.meta;

import java.util.*;

import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.BaseFactory;
import ai.libs.jaicore.graph.Graph;
import autofe.util.FilterUtils;

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
    public FilterPipeline getComponentInstantiation(final ComponentInstance groundComponent) {
        if (groundComponent == null || groundComponent.getComponent() == null) {
            return null;
        }

        Graph<IFilter> filterGraph = new Graph<>();
        Queue<ComponentInstance> open = new LinkedList<>();
        Map<ComponentInstance, IFilter> predFilters = new HashMap<>();

        if (groundComponent.getComponent().getName().equalsIgnoreCase("FilterPipeline")) {

            ComponentInstance actCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("pipe");
            if (actCI == null) {
                filterGraph.addItem(new IdentityFilter());
                return new FilterPipeline(groundComponent, filterGraph);
            }

            open.offer(actCI);
            predFilters.put(actCI, null);

            while (!open.isEmpty()) {
                actCI = open.poll();

                if (actCI == null) {
                    continue;
                }

                IFilter predFilter = predFilters.remove(actCI);

                switch (actCI.getComponent().getName()) {
                    case UNION_NAME:
                        handleUnionFilter(actCI, filterGraph, predFilter, open, predFilters);
                        break;

                    case PREP_PIPE_NAME:
                        ComponentInstance prepCI = actCI.getSatisfactionOfRequiredInterfaces().get("preprocessor");
                        if (prepCI == null) {
                            break;
                        }

                        IFilter prep = FilterUtils.getFilterForName(prepCI.getComponent().getName(),
                                prepCI.getParameterValues(), inputShape);

                        filterGraph.addItem(prep);
                        if (predFilter != null) {
                            filterGraph.addEdge(predFilter, prep);
                        }

                        ComponentInstance furtherCI = actCI.getSatisfactionOfRequiredInterfaces().get("further");
                        if (furtherCI != null) {
                            open.offer(furtherCI);
                            predFilters.put(furtherCI, prep);
                        }
                        break;

                    case ABSTRACT_PIPE_NAME:
                        ComponentInstance extractorCI = actCI.getSatisfactionOfRequiredInterfaces().get("extractor");
                        if (extractorCI == null) {
                            break;
                        }

                        IFilter extractor = FilterUtils.getFilterForName(extractorCI.getComponent().getName(),
                                extractorCI.getParameterValues(), inputShape);

                        filterGraph.addItem(extractor);
                        if (predFilter != null) {
                            filterGraph.addEdge(predFilter, extractor);
                        }

                        ComponentInstance preprocessorsCI = actCI.getSatisfactionOfRequiredInterfaces()
                                .get("preprocessors");
                        if (preprocessorsCI != null) {
                            open.offer(preprocessorsCI);
                            predFilters.put(preprocessorsCI, extractor);
                        }
                        break;

                    case NNPIPE_NAME:
                        ComponentInstance netCI = actCI.getSatisfactionOfRequiredInterfaces().get("net");
                        if (netCI == null) {
                            break;
                        }

                        IFilter net = FilterUtils.getFilterForName(netCI.getComponent().getName(),
                                netCI.getParameterValues(), inputShape);

                        filterGraph.addItem(net);
                        if (predFilter != null) {
                            filterGraph.addEdge(predFilter, net);
                        }

                        ComponentInstance prepsCI = actCI.getSatisfactionOfRequiredInterfaces().get("preprocessors");
                        if (prepsCI != null) {
                            open.offer(prepsCI);
                            predFilters.put(prepsCI, net);
                        }
                        break;
                    default:
                        handleDefaultCase(actCI, filterGraph, predFilter);
                        break;
                }
            }
            FilterPipeline result = new FilterPipeline(actCI, filterGraph);

            logger.info("Result pipeline after build: {}", result);
            return result;

        } else {
            logger.warn(
                    "Could not instantiate FilterPipeline object due to missing 'FilterPipeline' ground component.");
            return new FilterPipeline(groundComponent, filterGraph);
        }
    }

    private void handleUnionFilter(final ComponentInstance actCI, final Graph<IFilter> filterGraph,
                                   final IFilter predFilter, final Queue<ComponentInstance> open,
                                   final Map<ComponentInstance, IFilter> predFilters) {
        UnionFilter unionFilter = new UnionFilter();

        filterGraph.addItem(unionFilter);
        if (predFilter != null) {
            filterGraph.addEdge(predFilter, unionFilter);
        }

        ComponentInstance filter1CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter1");
        if (filter1CI != null) {
            open.offer(filter1CI);
            predFilters.put(filter1CI, unionFilter);
        }
        ComponentInstance filter2CI = actCI.getSatisfactionOfRequiredInterfaces().get("filter2");
        if (filter2CI != null) {
            open.offer(filter2CI);
            predFilters.put(filter2CI, unionFilter);
        }
    }

    private void handleDefaultCase(final ComponentInstance actCI, final Graph<IFilter> filterGraph,
                                   final IFilter predFilter) {
        IFilter basicFilter = FilterUtils.getFilterForName(actCI.getComponent().getName(),
                actCI.getParameterValues(), inputShape);

        filterGraph.addItem(basicFilter);
        if (predFilter != null) {
            filterGraph.addEdge(predFilter, basicFilter);
        }
    }

}
