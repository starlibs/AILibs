package ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

/**
 * Interface for the evaluator measure bridge yielding the measured value as an instance of O.
 *
 * @author mwever
 *
 * @param <V> The type of the measured value.
 */
public interface ISplitBasedClassifierEvaluator<V, I extends ILabeledInstance, D extends ILabeledDataset<I>> {

	/**
	 * Evaluate a hypothesis h being trained on a set of trainingData for some validationData.
	 * @param h The classifier to test for some training and validation data.
	 * @param trainingData The training data to train the classifier on.
	 * @param validationData The test data to validate the classifier on.
	 * @return Returns the result of the measured value.
	 * @throws Exception Throws an Exception if there are issues training or validating the classifier.
	 */
	public abstract V evaluateSplit(final IClassifier<I, D> h, D trainingData, D validationData) throws ObjectEvaluationFailedException, InterruptedException;

}
