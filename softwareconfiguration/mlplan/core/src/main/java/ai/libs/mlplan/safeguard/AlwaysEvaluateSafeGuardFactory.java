package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

public class AlwaysEvaluateSafeGuardFactory implements IEvaluationSafeGuardFactory {

	public AlwaysEvaluateSafeGuardFactory() {
		// nothing to do here
	}

	@Override
	public IEvaluationSafeGuardFactory withEvaluator(final ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> searchEvaluator) {
		// nothing to do here
		return this;
	}

	@Override
	public IEvaluationSafeGuard build() throws Exception {
		return new AlwaysEvaluateSafeGuard();
	}

}
