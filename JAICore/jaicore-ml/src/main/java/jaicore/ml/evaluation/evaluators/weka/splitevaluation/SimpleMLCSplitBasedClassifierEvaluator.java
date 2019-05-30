package jaicore.ml.evaluation.evaluators.weka.splitevaluation;

import java.util.LinkedList;
import java.util.List;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.core.evaluation.measure.IMeasure;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SimpleMLCSplitBasedClassifierEvaluator extends AbstractSplitBasedClassifierEvaluator<double[], Double> {

	public SimpleMLCSplitBasedClassifierEvaluator(final IMeasure<double[], Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final Classifier pl, final Instances trainingData, final Instances validationData) throws ObjectEvaluationFailedException, InterruptedException {
		try {
			pl.buildClassifier(trainingData);
			int numLabels = trainingData.classIndex();

			List<double[]> actual = new LinkedList<>();
			List<double[]> expected = new LinkedList<>();

			for (int i = 0; i < validationData.size(); i++) {
				actual.add(pl.distributionForInstance(validationData.get(i)));
				expected.add(MLUtils.toDoubleArray(validationData.get(i), numLabels));

				Double error = this.getBasicEvaluator().calculateAvgMeasure(actual, expected);

				if ((error + "").equals("NaN")) {
					throw new ObjectEvaluationFailedException("Classifier " + pl.getClass().getName() + " could not be evalauted. Please refer to the previous logs for more detailed information.");
				}
			}
			return this.getBasicEvaluator().calculateAvgMeasure(actual, expected);
		} catch (OutOfMemoryError e) {
			throw new ObjectEvaluationFailedException("Ran out of memory while building classifier " + ((MultiLabelClassifier) pl).getModel(), e);
		} catch (ObjectEvaluationFailedException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not train classifier");
		}
	}
}