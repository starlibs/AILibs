package autofe.algorithm.hasco;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.BaseFactory;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;

public class AutoFEPreferredNodeEvaluator implements IPathEvaluator<TFDNode, String,Double> {

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

	public FilterPipeline getPipelineFromNode(final BackPointerPath<TFDNode, String, ?> node) {
		if (this.components == null || this.factory == null) {
			throw new IllegalArgumentException(
					"Collection of components and factory need to be set to make node evaluators work.");
		}
		ComponentInstance ci = this.getComponentInstanceFromNode(node);

		try {
			return (ci == null) ? null : this.factory.getComponentInstantiation(ci);
		} catch (Exception e) {
			logger.warn("Could not instantiate component instance {} due to {}.", ci, e.getMessage());
			return null;
		}
	}

	public FilterPipeline getPipelineFromComponentInstance(final ComponentInstance ci) throws ComponentInstantiationFailedException {
		return this.factory.getComponentInstantiation(ci);
	}

	private ComponentInstance getComponentInstanceFromNode(final BackPointerPath<TFDNode, String,?> node) {
		return Util.getSolutionCompositionFromState(this.components, node.getHead().getState(), true);
	}

	@Override
	public Double f(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException {
		if (path.getNodes().size() == 1) {
			return 0.0;
		}

		List<String> remainingASTasks = path.getHead().getRemainingTasks().stream().map(Literal::getProperty)
				.filter(x -> x.startsWith("1_")).collect(Collectors.toList());
		String appliedMethod = (path.getHead().getAppliedMethodInstance() != null
				? path.getHead().getAppliedMethodInstance().getMethod().getName()
						: "");

		logger.debug("Remaining AS Tasks: {}. Applied method: {}", remainingASTasks, appliedMethod);
		boolean toDoHasAlgorithmSelection = path.getHead().getRemainingTasks().stream()
				.anyMatch(x -> x.getProperty().startsWith("1_"));

		if (toDoHasAlgorithmSelection) {
			FilterPipeline pipe;
			try {
				pipe = this.getPipelineFromComponentInstance(
						Util.getSolutionCompositionFromState(this.components, path.getHead().getState(), false));
			} catch (ComponentInstantiationFailedException e) {
				throw new PathEvaluationException("Node evaluation failed due to pipeline construction error.", e);
			}
			logger.debug("Todo has algorithm selection tasks. Calculate node evaluation for {}.", pipe);

			if (pipe == null || pipe.getFilters() == null || pipe.getFilters().getItems() == null
					|| pipe.getFilters().getItems().isEmpty()) {
				return 0.0;
			}

			if (pipe.getFilters().getItems().size() > this.maxPipelineSize) {
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
