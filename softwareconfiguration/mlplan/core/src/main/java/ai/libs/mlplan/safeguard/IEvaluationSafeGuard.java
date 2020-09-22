package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.mlplan.core.ITimeTrackingLearner;

/**
 * An evaluation safe guard can be used to predict whether an evaluation is likely to succeed or not.
 * Based on this information the evaluation may be adapted or omitted totally.
 *
 * Three types of runtimes are distinguished:
 * - induction time: The time needed to induce a model as described via the component instance.
 * - inference time: The time needed to do inference with a model as described via the component instance.
 * - evaluation time: The time needed to do both: induction and inference. Thus, it is the sum of the previous two runtimes.
 *
 * @author mwever
 */
public interface IEvaluationSafeGuard {

	public static final String ANNOTATION_PREDICTED_INDUCTION_TIME = "predictedInductionTime";
	public static final String ANNOTATION_PREDICTED_INFERENCE_TIME = "predictedInferenceTime";
	public static final String ANNOTATION_SOURCE = "predictionSource";

	/**
	 * Predicts whether a component instance <code>ci</code> is likely to adhere to the given <code>timeout</code>.
	 * @parma ci The component instance to make the prediction for.
	 * @param timeout The timeout posed to the evaluation of the component instance.
	 * @return Returns true iff the component instance can likely be evaluated within the given timeout.
	 * @throws Exception
	 */
	public boolean predictWillAdhereToTimeout(final IComponentInstance ci, Timeout timeout) throws Exception;

	/**
	 * Predicts the runtime that is required for inducing a model.
	 *
	 * @param ci The component instance describing the model to predict the induction time for.
	 * @param metaFeaturesTrain The meta features describing the data inducing a model from.
	 * @return The time needed for inducing a model.
	 * @throws Exception
	 */
	public double predictInductionTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain) throws Exception;

	/**
	 * Predicts the runtime that is required for doing inference with the given model.
	 *
	 * @param ci The component instance describing the model to predict the inference time for.
	 * @param metaFeaturesTest The meta features describing the data for which inference is to be done.
	 * @return The time needed for making predictions on the validation set.
	 * @throws Exception
	 */
	public double predictInferenceTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception;

	/**
	 * @param ci The component instance describing the model to predict the evaluation time for.
	 * @param metaFeaturesTrain The meta features describing the data to induce a model from.
	 * @param metaFeaturesTest The meta features describing the data to do inference for.
	 * @return The time needed for inducing a model and making predictions.
	 */
	default double predictEvaluationTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception {
		return this.predictInductionTime(ci, dTrain) + this.predictInferenceTime(ci, dTrain, dTest);
	}

	/**
	 * Updates the safe guard with current information obtained by measuring the induction and inference time of the given component instance on-line.
	 *
	 * @param ci The component instance describing the model to update the actual information for.
	 * @param wrappedLearner The learner that has been used to evaluate the component instance. It must be a time tracking learner.
	 */
	public void updateWithActualInformation(final IComponentInstance ci, final ITimeTrackingLearner wrappedLearner);

	public void registerListener(Object listener);
}
