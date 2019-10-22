package ai.libs.mlplan.core;

import java.util.Collection;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.hasco.model.Component;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public abstract class PipelineValidityCheckingNodeEvaluator<D extends ILabeledDataset<?>> implements IPathEvaluator<TFDNode, String, Double> {

	private D data;
	private Collection<Component> components;

	public PipelineValidityCheckingNodeEvaluator() {

	}

	public PipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final D data) {
		this.data = data;
		this.components = components;
	}

	public void setData(final D data) {
		this.data = data;
	}

	public void setComponents(final Collection<Component> components) {
		this.components = components;
	}

	public D getData() {
		return this.data;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}
}
