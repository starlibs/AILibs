package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.Arrays;
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
import weka.core.Instance;
import weka.core.Instances;

public class WekaPipelineValidityCheckingNodeEvaluator extends PipelineValidityCheckingNodeEvaluator {

	/* the predicates of the dataset */
	private boolean propertiesDetermined;
	private boolean binaryClass;
	private boolean multiClass;
	private boolean regression;
	private boolean multiValuedNominalAttributes;
	private boolean containsNegativeValues;

	public WekaPipelineValidityCheckingNodeEvaluator() {
		super();
	}

	public WekaPipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final Instances data) {
		super(components, data);
	}

	private boolean multiValuedNominalAttributesExist() {
		for (int i = 0; i < this.getData().numAttributes(); i++) {
			Attribute att = this.getData().attribute(i);
			if (att != this.getData().classAttribute() && att.isNominal() && att.numValues() > 2) {
				return true;
			}
		}
		return false;
	}

	private synchronized void extractDatasetProperties() {
		if (!this.propertiesDetermined) {
			/* compute binary class predicate */
			this.binaryClass = this.getData().classAttribute().isNominal() && this.getData().classAttribute().numValues() == 2;
			this.multiClass = this.getData().classAttribute().isNominal() && this.getData().classAttribute().numValues() > 2;
			this.regression = this.getData().classAttribute().isNumeric();

			/* determine whether the dataset is multi-valued nominal */
			this.multiValuedNominalAttributes = this.multiValuedNominalAttributesExist();

			this.containsNegativeValues = false;
			for (Instance i : this.getData()) {
				this.containsNegativeValues = this.containsNegativeValues || Arrays.stream(i.toDoubleArray()).anyMatch(x -> x < 0);
			}

			this.propertiesDetermined = true;
		}
	}

	@Override
	public Double f(final Node<TFDNode, ?> n) throws ControlledNodeEvaluationException {
		if (!this.propertiesDetermined) {
			this.extractDatasetProperties();
		}

		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.getComponents(), n.getPoint().getState(), false);
		if (instance != null) {

			/* check invalid preprocessor combinations */
			ComponentInstance pp = instance.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			if (pp != null && pp.getComponent().getName().contains("AttributeSelection")) {
				ComponentInstance search = pp.getSatisfactionOfRequiredInterfaces().get("search");
				ComponentInstance eval = pp.getSatisfactionOfRequiredInterfaces().get("eval");
				if (search != null && eval != null && !WekaUtil.isValidPreprocessorCombination(search.getComponent().getName(), eval.getComponent().getName())) {
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
				this.checkValidity(classifier.getComponent().getName().toLowerCase());
			}
		}
		return null;
	}

	private void checkValidity(final String classifierName) throws ControlledNodeEvaluationException {
		/* forbid M5regression algorithms on non-binary classes */
		boolean binaryClassifierMatch = classifierName.matches("(.*)(additiveregression|simplelinearregression|m5rules|votedperceptron|m5p)(.*)");

		if (!this.binaryClass && binaryClassifierMatch) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted on non-binary datasets.");
		}

		boolean noBinaryClassifierMatch = classifierName.matches("(.*)(additiveregression|m5p|m5rules|simplelinearregression)(.*)");
		if (this.binaryClass && noBinaryClassifierMatch) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted for binary classification tasks.");
		}

		/* forbid NaiveBayesMultinomial on multi-valued nominal attributes */
		if (this.multiValuedNominalAttributes && (classifierName.matches("(.*)(naivebayesmultinomial|simplelinearregression)(.*)"))) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted on datasets with multi-valued nominal attributes.");
		}

		boolean noMulticlassClassifierMatch = classifierName.matches("(.*)(votedperceptron)(.*)");
		if (this.multiClass && noMulticlassClassifierMatch) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted on multinomial classification dataset.");
		}

		if (this.regression && !classifierName.matches("(.*)(additiveregression|m5p|m5rules|simplelinearregression)(.*)")) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted on regression problems.");
		}

		if (this.containsNegativeValues && classifierName.matches("(.*)(naivebayesmultinomial)(.*)")) {
			throw new ControlledNodeEvaluationException("Negative numeric attribute values are not supported by the classifier.");
		}
	}

}
