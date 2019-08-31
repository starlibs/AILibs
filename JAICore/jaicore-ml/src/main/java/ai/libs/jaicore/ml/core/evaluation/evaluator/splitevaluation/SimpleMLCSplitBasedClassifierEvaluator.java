package ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation;

import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.dataset.IMultiLabelClassificationDataset;
import org.api4.java.ai.ml.classification.multilabel.dataset.IMultiLabelClassificationInstance;
import org.api4.java.ai.ml.classification.multilabel.learner.IMultiLabelClassifier;
import org.api4.java.ai.ml.core.evaluation.loss.IBatchLossFunction;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;

public class SimpleMLCSplitBasedClassifierEvaluator extends AbstractSplitBasedClassifierEvaluator<Double, IMultiLabelClassificationInstance, IMultiLabelClassificationDataset> {

	public SimpleMLCSplitBasedClassifierEvaluator(final IBatchLossFunction<Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final IMultiLabelClassifier pl, final IMultiLabelClassificationDataset trainingData, final IMultiLabelClassificationDataset validationData) throws ObjectEvaluationFailedException, InterruptedException {
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