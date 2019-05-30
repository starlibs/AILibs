package de.upb.crc901.mlplan.core;

import java.util.Collection;

import hasco.model.Component;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.core.Instances;

public abstract class PipelineValidityCheckingNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private Instances data;
	private Collection<Component> components;

	public PipelineValidityCheckingNodeEvaluator() {

	}

	public PipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final Instances data) {
		this.data = data;
		this.components = components;
	}

	public void setData(Instances data) {
		this.data = data;
	}

	public void setComponents(Collection<Component> components) {
		this.components = components;
	}

	public Instances getData() {
		return data;
	}

	public Collection<Component> getComponents() {
		return components;
	}
}
