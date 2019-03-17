package jaicore.ml.evaluation.evaluators.weka.measurebridge;

import java.util.LinkedList;
import java.util.List;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.core.evaluation.measure.IMeasure;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SimpleMLCEvaluatorMeasureBridge extends AbstractEvaluatorMeasureBridge<double[], Double> {

	public SimpleMLCEvaluatorMeasureBridge(final IMeasure<double[], Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final Classifier pl, final Instances trainingData, final Instances validationData) throws ObjectEvaluationFailedException, InterruptedException {
		try {
			pl.buildClassifier(trainingData);
		} catch (OutOfMemoryError e) {
			throw new ObjectEvaluationFailedException("Ran out of memory while building classifier " + ((MultiLabelClassifier) pl).getModel(), e);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not train classifier");
		}
		int numLabels = trainingData.classIndex();

		List<double[]> actual = new LinkedList<>();
		List<double[]> expected = new LinkedList<>();

		try {
			for (int i = 0; i < validationData.size(); i++) {
				actual.add(pl.distributionForInstance(validationData.get(i)));
				expected.add(MLUtils.toDoubleArray(validationData.get(i), numLabels));
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not validate trained classifier", e);
		}

		Double error = this.getBasicEvaluator().calculateAvgMeasure(actual, expected);

		if ((error + "").equals("NaN")) {
			throw new ObjectEvaluationFailedException("Classifier " + pl.getClass().getName() + " could not be evalauted. Please refer to the previous logs for more detailed information.");
		}

		return this.getBasicEvaluator().calculateAvgMeasure(actual, expected);
	}

}
