package jaicore.ml.evaluation.evaluators.weka;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.evaluation.IInstancesClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Basic implementation of the {@link AbstractEvaluatorMeasureBridge}. Uses the given loss function to compute loss on the given data. No extra steps are performed.
 * 
 * @author jnowack
 *
 */
public class SimpleEvaluatorMeasureBridge extends AbstractEvaluatorMeasureBridge<Double, Double>{

	public SimpleEvaluatorMeasureBridge(IMeasure<Double, Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(Classifier classifier, Instances trainingData, Instances validationData) throws Exception {
		List<Double> actual = WekaUtil.getClassesAsList(validationData);
		List<Double> predicted = new ArrayList<>();
		classifier.buildClassifier(trainingData);
		if (classifier instanceof IInstancesClassifier) {
			for (double prediction : ((IInstancesClassifier) classifier).classifyInstances(validationData)) {
				predicted.add(prediction);
			}
		} else {
			for (Instance inst : validationData) {
				predicted.add(classifier.classifyInstance(inst));
			}
		}
		return this.basicEvaluator.calculateAvgMeasure(actual, predicted);
	}

}
