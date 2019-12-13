package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

public interface ISupervisedLearnerEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> {
	public ISupervisedLearnerEvaluator<I, D> getLearnerEvaluator() throws LearnerEvaluatorConstructionFailedException;

}
