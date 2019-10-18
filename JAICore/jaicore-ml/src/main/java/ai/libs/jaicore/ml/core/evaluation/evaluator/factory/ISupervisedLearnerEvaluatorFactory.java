package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

public interface ISupervisedLearnerEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> {

	public ISupervisedLearnerEvaluator<I, D> getDataspecificRandomizedLearnerEvaluator(D dataset, ISupervisedLearnerMetric metric, Random random) throws LearnerEvaluatorConstructionFailedException;

}
