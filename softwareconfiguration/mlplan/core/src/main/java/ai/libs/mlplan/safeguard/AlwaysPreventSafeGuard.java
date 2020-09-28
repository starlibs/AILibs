package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.mlplan.core.ITimeTrackingLearner;

public class AlwaysPreventSafeGuard implements IEvaluationSafeGuard {

	@Override
	public boolean predictWillAdhereToTimeout(final IComponentInstance ci, final Timeout timeout) throws Exception {
		if (!(ci instanceof ComponentInstance)) {
			throw new IllegalArgumentException("Only works with ComponentInstance objects");
		}
		((ComponentInstance)ci).putAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INDUCTION_TIME, Integer.MAX_VALUE + "");
		((ComponentInstance)ci).putAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INFERENCE_TIME, Integer.MAX_VALUE + "");
		return false;
	}

	@Override
	public double predictInductionTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain) throws Exception {
		return Integer.MAX_VALUE;
	}

	@Override
	public double predictInferenceTime(final IComponentInstance ci, final ILabeledDataset<?> dTrain, final ILabeledDataset<?> dTest) throws Exception {
		return Integer.MAX_VALUE;
	}

	@Override
	public void updateWithActualInformation(final IComponentInstance ci, final ITimeTrackingLearner wrappedLearner) {
		// intentionally do nothing
	}

	@Override
	public void registerListener(final Object listener) {
		// intentionally do nothing
	}

}
