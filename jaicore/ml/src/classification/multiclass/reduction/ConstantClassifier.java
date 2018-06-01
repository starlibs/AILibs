package jaicore.ml.classification.multiclass.reduction;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class ConstantClassifier implements Classifier {

	@Override
	public void buildClassifier(Instances data) throws Exception {
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		return 0.0;
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return new double[] { 1.0 };
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

	public Classifier clone() {
		return new ConstantClassifier();
	}
}
