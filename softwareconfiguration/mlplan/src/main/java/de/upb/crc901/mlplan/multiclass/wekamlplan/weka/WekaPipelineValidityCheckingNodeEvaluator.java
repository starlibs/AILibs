package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.Collection;

import de.upb.crc901.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.ml.WekaUtil;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.exceptions.ControlledNodeEvaluationException;
import jaicore.search.model.travesaltree.Node;
import weka.core.Attribute;
import weka.core.Instances;

public class WekaPipelineValidityCheckingNodeEvaluator extends PipelineValidityCheckingNodeEvaluator {


	/* the predicates of the dataset */
	private boolean propertiesDetermined;
	private boolean binaryClass;
	private boolean multiValuedNominalAttributes;

	public WekaPipelineValidityCheckingNodeEvaluator() {
		super();
	}
	
	public WekaPipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final Instances data) {
		super(components, data);
	}
	
	@Override
	public Double f(final Node<TFDNode, ?> n) throws ControlledNodeEvaluationException {
		
		if (!propertiesDetermined) {
			propertiesDetermined = true;
			
			/* compute binary class predicate */
			this.binaryClass = getData().numClasses() == 2;
			
			/* determine whether the dataset is multi-valued nominal */
			boolean multiValuedNominalAttributes = false;
			for (int i = 0; i < getData().numAttributes(); i++) {
				Attribute att = getData().attribute(i);
				if (att != getData().classAttribute()) {
					if (att.isNominal() && att.numValues() > 2) {
						multiValuedNominalAttributes = true;
					}
				}
			}
			this.multiValuedNominalAttributes = multiValuedNominalAttributes;
		}
		
		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(getComponents(), n.getPoint().getState(), false);
		if (instance != null) {
			
			/* check invalid preprocessor combinations */
			ComponentInstance pp = instance.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			if (pp != null && pp.getComponent().getName().contains("AttributeSelection")) {
				ComponentInstance search = pp.getSatisfactionOfRequiredInterfaces().get("search");
				ComponentInstance eval = pp.getSatisfactionOfRequiredInterfaces().get("eval");
				if (search != null && eval != null) {
					if (!WekaUtil.isValidPreprocessorCombination(search.getComponent().getName(), eval.getComponent().getName()))
						throw new ControlledNodeEvaluationException("The given combination of searcher and evaluator cannot be benchmarked since they are incompatible.");
				}
			}
			
			/* check invalid classifiers for this kind of dataset */
			ComponentInstance classifier;
			if (instance.getComponent().getName().toLowerCase().contains("pipeline")) {
				classifier = instance.getSatisfactionOfRequiredInterfaces().get("classifier");
			} else {
				classifier = instance;
			}

			if (classifier != null) {
				String classifierName = classifier.getComponent().getName().toLowerCase();

				/* forbid M5regression algorithms on non-binary classes */
				if (!this.binaryClass && classifierName.matches("(.*)(additiveregression|simplelinearregression|m5rules|votedperceptron|m5p)(.*)")) {
					throw new ControlledNodeEvaluationException("Cannot adopt classifier " + classifier.getComponent().getName() + " on non-binary datasets.");
				}

				/* forbid NaiveBayesMultinomial on multi-valued nominal attributes */
				if (this.multiValuedNominalAttributes && (classifierName.matches("(.*)(naivebayesmultinomial|simplelinearregression)(.*)"))) {
					throw new ControlledNodeEvaluationException("Cannot adopt classifier " + classifier.getComponent().getName() + " on datasets with multi-valued nominal attributes.");
				}
			}
		}
		return null;
	}

}
