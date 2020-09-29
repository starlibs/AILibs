package ai.libs.mlplan.weka.weka;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.ControlledNodeEvaluationException;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;

public class WekaPipelineValidityCheckingNodeEvaluator extends PipelineValidityCheckingNodeEvaluator implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(WekaPipelineValidityCheckingNodeEvaluator.class);

	public WekaPipelineValidityCheckingNodeEvaluator() {
		super();
	}

	public WekaPipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final ILabeledDataset<?> data) {
		super(components, data);
	}

	@Override
	public Double evaluate(final ILabeledPath<TFDNode, String> path) throws ControlledNodeEvaluationException {
		if (!this.propertiesDetermined) {
			this.extractDatasetProperties();
		}

		/* get partial component */
		ComponentInstance instance = HASCOUtil.getSolutionCompositionFromState(this.getComponents(), path.getHead().getState(), false);
		if (instance != null) {

			/* check invalid preprocessor combinations */
			if (instance.getSatisfactionOfRequiredInterfaces().containsKey("preprocessor")) {
				IComponentInstance pp = instance.getSatisfactionOfRequiredInterface("preprocessor").iterator().next();
				if (pp != null && pp.getComponent().getName().contains("AttributeSelection")) {
					IComponentInstance search = pp.getSatisfactionOfRequiredInterface("search").iterator().next();
					IComponentInstance eval = pp.getSatisfactionOfRequiredInterface("eval").iterator().next();
					if (search != null && eval != null && !WekaUtil.isValidPreprocessorCombination(search.getComponent().getName(), eval.getComponent().getName())) {
						throw new ControlledNodeEvaluationException("The given combination of searcher and evaluator cannot be benchmarked since they are incompatible.");
					}
				}
			}

			/* check invalid classifiers for this kind of dataset */
			IComponentInstance classifier;
			if (instance.getComponent().getName().toLowerCase().contains("pipeline")) {
				classifier = instance.getSatisfactionOfRequiredInterface("classifier").iterator().next();
			} else {
				classifier = instance;
			}

			if (classifier != null) {
				this.checkValidity(classifier);
			}
		}
		return null;
	}

	private void checkValidity(final IComponentInstance classifier) throws ControlledNodeEvaluationException {
		String classifierName = classifier.getComponent().getName().toLowerCase();

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

		if (this.containsNegativeValues && classifierName.matches("(.*)(naivebayesmultinomial)(.*)")) {
			throw new ControlledNodeEvaluationException("Negative numeric attribute values are not supported by the classifier.");
		}

		/* Exclude some learners for regression problems */
		if (this.regression && classifierName.matches("(.*)(oner|smo|j48|jrip|naivebayes|logistic|lmt|bayesnet)(.*)")) {
			throw new ControlledNodeEvaluationException("Learner does not support regression");
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
