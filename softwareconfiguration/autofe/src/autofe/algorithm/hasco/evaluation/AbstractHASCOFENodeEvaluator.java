package autofe.algorithm.hasco.evaluation;

import java.util.Collection;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.query.Factory;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public abstract class AbstractHASCOFENodeEvaluator extends AbstractHASCOFEEvaluator implements INodeEvaluator<TFDNode, Double> {

	private Collection<Component> components;
	private Factory<FilterPipeline> factory;

	// Maximum size of a pipeline
	protected int maxPipelineSize;

	public AbstractHASCOFENodeEvaluator(final int maxPipelineSize) {
		this.maxPipelineSize = maxPipelineSize;
	}

	public void setComponents(final Collection<Component> components) {
		this.components = components;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public void setFactory(final Factory<FilterPipeline> factory) {
		this.factory = factory;
	}

	public Factory<FilterPipeline> getFactory() {
		return this.factory;
	}

	public FilterPipeline getPipelineFromNode(final Node<TFDNode, ?> node) throws Exception {
		if (this.components == null || this.factory == null) {
			throw new IllegalArgumentException("Collection of components and factory need to be set to make node evaluators work.");
		}

		ComponentInstance ci = Util.getSolutionCompositionFromState(this.getComponents(), node.getPoint().getState());
		if (ci == null) {
			return null;
		}
		return this.getFactory().getComponentInstantiation(ci);
	}
}
