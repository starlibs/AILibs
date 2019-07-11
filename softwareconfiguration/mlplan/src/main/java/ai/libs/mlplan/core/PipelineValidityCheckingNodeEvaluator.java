package ai.libs.mlplan.core;

import java.util.Collection;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.hasco.model.Component;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import weka.core.Instances;

public abstract class PipelineValidityCheckingNodeEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private Instances data;
	private Collection<Component> components;

	public PipelineValidityCheckingNodeEvaluator() {

	}

	public PipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final Instances data) {
		this.data = data;
		this.components = components;
	}

	public void setData(final Instances data) {
		this.data = data;
	}

	public void setComponents(final Collection<Component> components) {
		this.components = components;
	}

	public Instances getData() {
		return this.data;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}
}
