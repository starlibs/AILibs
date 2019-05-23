package jaicore.ml.evaluation.evaluators.weka;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.IInstancesClassifier;
import jaicore.ml.evaluation.measures.IMeasure;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class SimpleEvaluatorMeasureBridge extends AbstractEvaluatorMeasureBridge<Double, Double>{

	public SimpleEvaluatorMeasureBridge(IMeasure<Double, Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(Classifier pl, Instances trainingData, Instances validationData) throws Exception {
		List<Double> actual = WekaUtil.getClassesAsList(validationData);
		List<Double> predicted = new ArrayList<>();
		pl.buildClassifier(trainingData);
		if (pl instanceof IInstancesClassifier) {
			for (double prediction : ((IInstancesClassifier) pl).classifyInstances(validationData)) {
				predicted.add(prediction);
			}
		} else {
			for (Instance inst : validationData) {
				predicted.add(pl.classifyInstance(inst));
			}
		}
		return this.basicEvaluator.calculateAvgMeasure(actual, predicted);
	}

}
