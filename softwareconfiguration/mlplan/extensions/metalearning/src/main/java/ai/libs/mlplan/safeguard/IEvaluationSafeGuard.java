package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.hasco.model.ComponentInstance;

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

	/**
	 * Predicts the runtime that is required for inducing a model.
	 *
	 * @param ci The component instance describing the model to predict the induction time for.
	 * @param metaFeaturesTrain The meta features describing the data inducing a model from.
	 * @return The time needed for inducing a model.
	 * @throws Exception
	 */
	public double predictInductionTime(final ComponentInstance ci, final ILabeledDataset<?> dTrain) throws Exception;

	/**
	 * Predicts the runtime that is required for doing inference with the given model.
	 *
	 * @param ci The component instance describing the model to predict the inference time for.
	 * @param metaFeaturesTest The meta features describing the data for which inference is to be done.
	 * @return The time needed for making predictions on the validation set.
	 * @throws Exception
	 */
	public double predictInferenceTime(final ComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception;

	/**
	 * @param ci The component instance describing the model to predict the evaluation time for.
	 * @param metaFeaturesTrain The meta features describing the data to induce a model from.
	 * @param metaFeaturesTest The meta features describing the data to do inference for.
	 * @return The time needed for inducing a model and making predictions.
	 */
	default double predictEvaluationTime(final ComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception {
		return this.predictInductionTime(ci, dTrain) + this.predictInferenceTime(ci, dTrain, dTest);
	}

	/**
	 * Updates the safe guard with current information obtained by measuring the induction and inference time of the given component instance on-line.
	 *
	 * @param ci The component instance describing the model to update the actual information for.
	 * @param inductionTime The induction time measured for the provided component instance.
	 * @param inferenceTime The inference time measured for the provided component instance.
	 */
	public void updateWithActualInformation(final ComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest, final double inductionTime, final double inferenceTime);
}
