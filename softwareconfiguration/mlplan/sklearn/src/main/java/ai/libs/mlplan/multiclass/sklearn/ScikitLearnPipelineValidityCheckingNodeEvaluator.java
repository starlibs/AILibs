package ai.libs.mlplan.multiclass.sklearn;

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
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.ControlledNodeEvaluationException;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;

public class ScikitLearnPipelineValidityCheckingNodeEvaluator extends PipelineValidityCheckingNodeEvaluator implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(ScikitLearnPipelineValidityCheckingNodeEvaluator.class);

	public ScikitLearnPipelineValidityCheckingNodeEvaluator() {
		super();
	}

	public ScikitLearnPipelineValidityCheckingNodeEvaluator(final Collection<Component> components, final ILabeledDataset<?> data) {
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

		if (this.containsNegativeValues && classifierName.matches("(.*)(multinomialnb)(.*)")) {
			throw new ControlledNodeEvaluationException("Negative numeric attribute values are not supported by the classifier.");
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
