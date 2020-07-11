package ai.libs.mlplan.core;

import java.util.Collection;
import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public abstract class PipelineValidityCheckingNodeEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private final ILabeledDataset<?> data;
	private final Collection<Component> components;

	public PipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final ILabeledDataset<?> data) {
		Objects.requireNonNull(components);
		Objects.requireNonNull(data);
		this.data = data;
		this.components = components;
	}

	public ILabeledDataset<?> getData() {
		return this.data;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}
}
