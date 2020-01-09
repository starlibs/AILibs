package ai.libs.mlplan.multiclass.wekamlplan.weka;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.ControlledNodeEvaluationException;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaPipelineValidityCheckingNodeEvaluator extends PipelineValidityCheckingNodeEvaluator implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(WekaPipelineValidityCheckingNodeEvaluator.class);

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
		super(components, new WekaInstances(data));
		Objects.requireNonNull(components);
		Objects.requireNonNull(data);
		components.forEach(c -> this.logger.info("Considering component {}", c));
	}

	private boolean multiValuedNominalAttributesExist() {
		Instances data = this.getData().getInstances();
		for (int i = 0; i < data.numAttributes(); i++) {
			Attribute att = data.attribute(i);
			if (att != data.classAttribute() && att.isNominal() && att.numValues() > 2) {
				return true;
			}
		}
		return false;
	}

	private synchronized void extractDatasetProperties() {
		if (!this.propertiesDetermined) {

			if (this.getComponents() == null) {
				throw new IllegalStateException("Components not defined!");
			}

			/* compute binary class predicate */
			Instances data = this.getInstancesInWekaFormat();
			this.binaryClass = data.classAttribute().isNominal() && data.classAttribute().numValues() == 2;
			this.multiClass = data.classAttribute().isNominal() && data.classAttribute().numValues() > 2;
			this.regression = data.classAttribute().isNumeric();

			/* determine whether the dataset is multi-valued nominal */
			this.multiValuedNominalAttributes = this.multiValuedNominalAttributesExist();

			this.containsNegativeValues = false;
			for (Instance i : data) {
				this.containsNegativeValues = this.containsNegativeValues || Arrays.stream(i.toDoubleArray()).anyMatch(x -> x < 0);
			}

			this.propertiesDetermined = true;
		}
	}

	@Override
	public Double evaluate(final ILabeledPath<TFDNode, String> path) throws ControlledNodeEvaluationException {
		if (!this.propertiesDetermined) {
			this.extractDatasetProperties();
		}

		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.getComponents(), path.getHead().getState(), false);
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
				this.checkValidity(classifier);
			}
		}
		return null;
	}

	private void checkValidity(final ComponentInstance classifier) throws ControlledNodeEvaluationException {

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

		if (this.regression && !classifierName.matches("(.*)(additiveregression|m5p|m5rules|simplelinearregression)(.*)")) {
			throw new ControlledNodeEvaluationException(classifierName + " cannot be adopted on regression problems.");
		}

		if (this.containsNegativeValues && classifierName.matches("(.*)(naivebayesmultinomial)(.*)")) {
			throw new ControlledNodeEvaluationException("Negative numeric attribute values are not supported by the classifier.");
		}
	}

	@Override
	public WekaInstances getData() {
		return (WekaInstances)super.getData();
	}

	public Instances getInstancesInWekaFormat() {
		return this.getData().getInstances();
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
