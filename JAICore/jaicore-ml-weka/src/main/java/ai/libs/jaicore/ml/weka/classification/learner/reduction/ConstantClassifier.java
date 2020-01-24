package ai.libs.jaicore.ml.weka.classification.learner.reduction;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class ConstantClassifier implements Classifier {

	private static final long serialVersionUID = 8190066987365474681L;

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		/* does nothing */
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return 0.0;
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return new double[] { 1.0 };
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

	@Override
	public Classifier clone() {
		return new ConstantClassifier();
	}
}
