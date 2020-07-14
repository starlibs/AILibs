package ai.libs.mlplan.core;

import java.util.Collection;
import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public abstract class PipelineValidityCheckingNodeEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private ILabeledDataset<?> data;
	private Collection<Component> components;

	public PipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final ILabeledDataset<?> data) {
		this.setComponents(components);
		this.setData(data);
	}

	public PipelineValidityCheckingNodeEvaluator() {

	}

	public void setData(final ILabeledDataset<?> data) {
		Objects.requireNonNull(data);
		this.data = data;
	}

	public void setComponents(final Collection<Component> components) {
		Objects.requireNonNull(components);
		this.components = components;
	}

	public ILabeledDataset<?> getData() {
		return this.data;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}
}
