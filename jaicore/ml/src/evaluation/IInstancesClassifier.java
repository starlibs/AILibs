package jaicore.ml.evaluation;

import weka.core.Instances;

public interface IInstancesClassifier {
	public double[] classifyInstances(Instances instances) throws Exception;
}
