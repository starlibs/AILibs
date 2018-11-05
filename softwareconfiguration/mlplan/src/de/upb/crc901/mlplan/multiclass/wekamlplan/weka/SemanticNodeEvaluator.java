package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.Collection;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import weka.core.Attribute;
import weka.core.Instances;

public class SemanticNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private final Instances data;
	private final Collection<Component> components;

	/* the predicates of the dataset */
	private final boolean binaryClass;
	private final boolean multiValuedNominalAttributes;

	public SemanticNodeEvaluator(final Collection<Component> components, final Instances data) {
		this.data = data;
		this.components = components;

		/* compute binary class predicate */
		binaryClass = this.data.numClasses() == 2;
		
		/* determine whether the dataset is multi-valued nominal */
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
	}

	@Override
	public Double f(final Node<TFDNode, ?> n) throws Exception {
		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.components, n.getPoint().getState(), false);

		if (instance != null) {
			ComponentInstance classifier;

			if (instance.getComponent().getName().toLowerCase().contains("pipeline")) {
				classifier = instance.getSatisfactionOfRequiredInterfaces().get("classifier");
			} else {
				classifier = instance;
			}
			
			if (classifier != null) {
				
				String classifierName = classifier.getComponent().getName().toLowerCase();
				
				/* forbid M5regression algorithms on non-binary classes */
				if (!this.binaryClass && classifierName.matches("(.*)(additiveregression|simplelinearregression|m5rules|votedperceptron|m5p)(.*)"))
					throw new IllegalArgumentException("Cannot adopt classifier " + classifier.getClass().getName() + " on non-binary datasets.");
				
				/* forbid NaiveBayesMultinomial on multi-valued nominal attributes */
				if (multiValuedNominalAttributes && (classifierName.matches("(.*)(naivebayesmultinomial|simplelinearregression)(.*)"))) {
					throw new IllegalArgumentException("Cannot adopt classifier " + classifier.getClass().getName() + " on datasets with multi-valued nominal attributes.");
				}
			}
		}

		return null;
	}

}
