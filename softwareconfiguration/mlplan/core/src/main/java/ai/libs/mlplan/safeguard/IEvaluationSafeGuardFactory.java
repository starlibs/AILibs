package ai.libs.mlplan.safeguard;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

public interface IEvaluationSafeGuardFactory {

	public IEvaluationSafeGuardFactory withEvaluator(ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> searchEvaluator);

	public IEvaluationSafeGuard build() throws Exception;

}
