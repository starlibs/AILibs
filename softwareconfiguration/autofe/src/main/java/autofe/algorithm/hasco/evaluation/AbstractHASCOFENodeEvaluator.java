package autofe.algorithm.hasco.evaluation;

import java.util.Collection;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.BaseFactory;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;

public abstract class AbstractHASCOFENodeEvaluator extends AbstractHASCOFEEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private Collection<Component> components;
	private BaseFactory<FilterPipeline> factory;

	// Maximum size of a pipeline
	protected int maxPipelineSize;

	protected AbstractHASCOFENodeEvaluator(final int maxPipelineSize) {
		this.maxPipelineSize = maxPipelineSize;
	}

	public void setComponents(final Collection<Component> components) {
		this.components = components;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public void setFactory(final BaseFactory<FilterPipeline> factory) {
		this.factory = factory;
	}

	public BaseFactory<FilterPipeline> getFactory() {
		return this.factory;
	}

	FilterPipeline getPipelineFromNode(final TFDNode node) throws ComponentInstantiationFailedException {
		if (this.components == null || this.factory == null) {
			throw new IllegalArgumentException("Collection of components and factory need to be set to make node evaluators work.");
		}

		ComponentInstance ci = Util.getSolutionCompositionFromState(this.getComponents(), node.getState(), true);
		if (ci == null) {
			return null;
		}
		return this.getFactory().getComponentInstantiation(ci);
	}

	protected FilterPipeline extractPipelineFromNode(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException {
		FilterPipeline pipe;
		try {
			pipe = this.getPipelineFromNode(path.getHead());
		} catch (ComponentInstantiationFailedException e1) {
			throw new PathEvaluationException("Could not evaluate pipeline.", e1);
		}
		return pipe;
	}

	boolean hasPathEmptyParent(final ILabeledPath<TFDNode, String> path) {
		return path.getNodes().size() == 1;
	}

	boolean hasPathExceededPipelineSize(final ILabeledPath<TFDNode, String> path) {
		return path.getNodes().size() > this.maxPipelineSize;
	}
}
