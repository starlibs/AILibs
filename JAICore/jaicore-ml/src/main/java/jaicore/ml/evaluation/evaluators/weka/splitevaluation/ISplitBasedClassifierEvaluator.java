package jaicore.ml.evaluation.evaluators.weka.splitevaluation;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Interface for the evaluator measure bridge yielding the measured value as an instance of O.
 *
 * @author mwever
 *
 * @param <O> The type of the measured value.
 */
public interface ISplitBasedClassifierEvaluator<O> {

	/**
	 * Evaluate a hypothesis h being trained on a set of trainingData for some validationData.
	 * @param h The classifier to test for some training and validation data.
	 * @param trainingData The training data to train the classifier on.
	 * @param validationData The test data to validate the classifier on.
	 * @return Returns the result of the measured value.
	 * @throws Exception Throws an Exception if there are issues training or validating the classifier.
	 */
	public abstract O evaluateSplit(final Classifier h, Instances trainingData, Instances validationData) throws ObjectEvaluationFailedException, InterruptedException;

}
