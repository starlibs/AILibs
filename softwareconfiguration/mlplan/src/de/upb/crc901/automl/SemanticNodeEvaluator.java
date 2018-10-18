package de.upb.crc901.automl;

import java.util.Collection;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;
import weka.core.Attribute;
import weka.core.Instances;

public class SemanticNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private INodeEvaluator<TFDNode, Double> wrappedNodeEvaluator;
	private final Instances data;
	private final Collection<Component> components;

	private final boolean multiValuedNominalAttributes;

	public SemanticNodeEvaluator(final Collection<Component> components, final Instances data) {
		this.data = data;
		this.components = components;

		boolean multiValuedNominalAttributes = false;
		for (int i = 0; i < this.data.numAttributes(); i++) {
			Attribute att = this.data.attribute(i);
			if (att != this.data.classAttribute()) {
				if (att.isNominal() && att.numValues() > 2) {
					multiValuedNominalAttributes = true;
				}
			}
		}
		this.multiValuedNominalAttributes = multiValuedNominalAttributes;

		System.out.println("Data has multi-valued nominal attributes: " + this.multiValuedNominalAttributes);
	}

	public SemanticNodeEvaluator(final Collection<Component> components, final Instances data, final INodeEvaluator<TFDNode, Double> wrappedNodeEvaluator) {
		this(components, data);
		this.wrappedNodeEvaluator = wrappedNodeEvaluator;
	}

	@Override
	public Double f(final Node<TFDNode, ?> n) throws Throwable {
		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.components, n.getPoint().getState());

		if (instance != null) {
			ComponentInstance classifier;

			if (instance.getComponent().getName().toLowerCase().contains("pipeline")) {
				classifier = instance.getSatisfactionOfRequiredInterfaces().get("classifier");
			} else {
				classifier = instance;
			}

			if (classifier != null) {
				if ((classifier.getComponent().getName().toLowerCase().contains("naivebayesmultinomial") && this.multiValuedNominalAttributes)
						|| (classifier.getComponent().getName().toLowerCase().contains("simplelinearregression") && this.multiValuedNominalAttributes)) {
					return 40000d;
				}
			}
		}

		if (this.wrappedNodeEvaluator != null)

		{
			return this.wrappedNodeEvaluator.f(n);
		} else {
			return 0.0;
		}
	}

}
