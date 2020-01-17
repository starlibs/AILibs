package ai.libs.jaicore.ml.weka.classification;

import org.api4.java.ai.ml.core.exception.PredictionException;

import weka.core.Instances;

public interface IInstancesClassifier {

	public double[] classifyInstances(Instances instances) throws PredictionException;

}
