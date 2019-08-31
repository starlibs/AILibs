package ai.libs.jaicore.ml.weka.classification;

import weka.core.Instances;

public interface IInstancesClassifier {

	public double[] classifyInstances(Instances instances) throws Exception;

}
