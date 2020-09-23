package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.mlplan.core.ITimeTrackingLearner;

/**
 * The AlwaysEvaluateSafeGuard is more of a dummy encoding of a safe guard that will always predict that any algorithm will be evaluated within the timeout and return a result in instant time. It thus can be employed to disable the safe guard feature of the {@link ai.libs.mlplan.core.PipelineEvaluator}.
 *
 * @author mwever
 */
public class AlwaysEvaluateSafeGuard implements IEvaluationSafeGuard {

	/**
	 * Standard constructor for initializing an AlwaysEvaluateSafeGuard.
	 */
	public AlwaysEvaluateSafeGuard() {
		// nothing to do here as the dummy safe guard is a static one.
	}

	@Override
	public boolean predictWillAdhereToTimeout(final IComponentInstance ci, final Timeout timeout) throws Exception {
		if (!(ci instanceof ComponentInstance)) {
			throw new IllegalArgumentException("Only works with ComponentInstance objects");
		}
		((ComponentInstance)ci).putAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INDUCTION_TIME, "0.0");
		((ComponentInstance)ci).putAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INFERENCE_TIME, "0.0");
		// always predict that it will adhere to the timeout, no matter what timeout is given.
		return true;
	}

	@Override
	public double predictInductionTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain) throws Exception {
		// predict it will be induced instantly.
		return 0;
	}

	@Override
	public double predictInferenceTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception {
		// predict it will be induced instantly.
		return 0;
	}

	@Override
	public void updateWithActualInformation(final IComponentInstance ci, final ITimeTrackingLearner learner) {
		// nothing to remember here
	}

	@Override
	public void registerListener(final Object listener) {
		// nothing to register at this point.
	}
}
