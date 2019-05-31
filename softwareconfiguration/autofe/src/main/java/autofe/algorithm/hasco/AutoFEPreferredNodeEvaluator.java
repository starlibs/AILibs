package autofe.algorithm.hasco;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.BaseFactory;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.model.travesaltree.Node;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;

public class AutoFEPreferredNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

    private static final Logger logger = LoggerFactory.getLogger(AutoFEPreferredNodeEvaluator.class);

    protected static final double ATT_COUNT_PENALTY = 1;

    public static final double MAX_EVAL_VALUE = 20000d;

    private static final int MINIMUM_FILTERS = 2;

    private Collection<Component> components;
    private BaseFactory<FilterPipeline> factory;

    // Maximum size of a pipeline
    protected int maxPipelineSize;

    public AutoFEPreferredNodeEvaluator(final Collection<Component> components,
                                        final BaseFactory<FilterPipeline> factory, final int maxPipelineSize) {
        this.components = components;
        this.maxPipelineSize = maxPipelineSize;
        this.factory = factory;
    }

    public FilterPipeline getPipelineFromNode(final Node<TFDNode, ?> node) {
        if (components == null || factory == null) {
            throw new IllegalArgumentException(
                    "Collection of components and factory need to be set to make node evaluators work.");
        }
        ComponentInstance ci = getComponentInstanceFromNode(node);

        try {
            return (ci == null) ? null : factory.getComponentInstantiation(ci);
        } catch (Exception e) {
            logger.warn("Could not instantiate component instance {} due to {}.", ci, e.getMessage());
            return null;
        }
    }

    public FilterPipeline getPipelineFromComponentInstance(final ComponentInstance ci) throws ComponentInstantiationFailedException {
        return factory.getComponentInstantiation(ci);
    }

    private ComponentInstance getComponentInstanceFromNode(final Node<TFDNode, ?> node) {
        return Util.getSolutionCompositionFromState(components, node.getPoint().getState(), true);
    }

    @Override
    public Double f(final Node<TFDNode, ?> node) throws NodeEvaluationException {
        if (node.getParent() == null) {
            return 0.0;
        }

        List<String> remainingASTasks = node.getPoint().getRemainingTasks().stream().map(Literal::getProperty)
                .filter(x -> x.startsWith("1_")).collect(Collectors.toList());
        String appliedMethod = (node.getPoint().getAppliedMethodInstance() != null
                ? node.getPoint().getAppliedMethodInstance().getMethod().getName()
                : "");

        logger.debug("Remaining AS Tasks: {}. Applied method: {}", remainingASTasks, appliedMethod);
        boolean toDoHasAlgorithmSelection = node.getPoint().getRemainingTasks().stream()
                .anyMatch(x -> x.getProperty().startsWith("1_"));

        if (toDoHasAlgorithmSelection) {
            FilterPipeline pipe;
            try {
                pipe = getPipelineFromComponentInstance(
                        Util.getSolutionCompositionFromState(components, node.getPoint().getState(), false));
            } catch (ComponentInstantiationFailedException e) {
                throw new NodeEvaluationException(e, "Node evaluation failed due to pipeline construction error.");
            }
            logger.debug("Todo has algorithm selection tasks. Calculate node evaluation for {}.", pipe);

            if (pipe == null || pipe.getFilters() == null || pipe.getFilters().getItems() == null
                    || pipe.getFilters().getItems().isEmpty()) {
                return 0.0;
            }

            if (pipe.getFilters().getItems().size() > maxPipelineSize) {
                return MAX_EVAL_VALUE;
            }

            if (pipe.getFilters().getItems().size() >= MINIMUM_FILTERS) {
                return null;
            }

            return 0.0;
        } else {
            return null;
        }
    }

}
