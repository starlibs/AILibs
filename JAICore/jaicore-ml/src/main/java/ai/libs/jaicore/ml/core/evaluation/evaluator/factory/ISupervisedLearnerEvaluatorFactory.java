package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMetric;

public interface ISupervisedLearnerEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> {
	public ISupervisedLearnerEvaluator<I, D> getDataspecificRandomizedLearnerEvaluator(D dataset, IAggregatedPredictionPerformanceMetric metric, Random random) throws LearnerEvaluatorConstructionFailedException;

}
