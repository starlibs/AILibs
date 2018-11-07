package autofe.algorithm.hasco;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.BaseFactory;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public class AutoFEPreferredNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private static final Logger logger = LoggerFactory.getLogger(AutoFEPreferredNodeEvaluator.class);

	protected static final double ATT_COUNT_PENALTY = 1;

	public static final double MAX_EVAL_VALUE = 20000d;

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
		if (this.components == null || this.factory == null) {
			throw new IllegalArgumentException(
					"Collection of components and factory need to be set to make node evaluators work.");
		}
		ComponentInstance ci = this.getComponentInstanceFromNode(node);

		try {
			return (ci == null) ? null : this.factory.getComponentInstantiation(ci);
		} catch (Exception e) {
			logger.warn("Could not instantiate component instance '" + ci + "' due to '" + e.getMessage() + "'.");
			return null;
		}
	}

	public FilterPipeline getPipelineFromComponentInstance(ComponentInstance ci) throws Exception {
		return this.factory.getComponentInstantiation(ci);
	}

	private ComponentInstance getComponentInstanceFromNode(final Node<TFDNode, ?> node) {
		return Util.getSolutionCompositionFromState(this.components, node.getPoint().getState(), true);
	}

	@Override
	public Double f(final Node<TFDNode, ?> node) throws Exception {
		if (node.getParent() == null) {
			return 0.0;
		}

		List<String> remainingASTasks = node.getPoint().getRemainingTasks().stream().map(x -> x.getProperty())
				.filter(x -> x.startsWith("1_")).collect(Collectors.toList());
		String appliedMethod = (node.getPoint().getAppliedMethodInstance() != null
				? node.getPoint().getAppliedMethodInstance().getMethod().getName()
				: "");

		logger.debug("Remaining AS Tasks: " + remainingASTasks + " applied method: " + appliedMethod);
		boolean toDoHasAlgorithmSelection = node.getPoint().getRemainingTasks().stream()
				.anyMatch(x -> x.getProperty().startsWith("1_"));

		if (toDoHasAlgorithmSelection) {
			// TODO: Resolve true oder false (false gives better results given less time)
			FilterPipeline pipe = this.getPipelineFromComponentInstance(
					Util.getSolutionCompositionFromState(this.components, node.getPoint().getState(), false));
			logger.debug("Todo has algorithm selection tasks {} Calculate node evaluation for {}.", pipe);

			if (pipe != null && pipe.getFilters() != null
					&& pipe.getFilters().getItems().size() > this.maxPipelineSize) {
				return MAX_EVAL_VALUE;
			}
			return 0.0;
		} else {
			return null;
		}
	}

}
