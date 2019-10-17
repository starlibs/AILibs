package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public interface ISupervisedLearnerEvaluatorFactory<D extends ILabeledDataset<?>, L extends ISupervisedLearner<?, D>> {

	public ISupervisedLearnerEvaluator<L> getIClassifierEvaluator(D dataset, long seed) throws ClassifierEvaluatorConstructionFailedException;

}
