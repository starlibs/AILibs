package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;

/**
 * Factory for configuring standard Monte Carlo cross-validation evaluators.
 *
 * @author mwever
 * @author fmohr
 *
 */
public class MonteCarloCrossValidationEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends AMonteCarloCrossValidationBasedEvaluatorFactory<I, D, MonteCarloCrossValidationEvaluatorFactory<I, D>> {

	@Override
	public MonteCarloCrossValidationEvaluator<I, D> getDataspecificRandomizedLearnerEvaluator(final D dataset, final ISupervisedLearnerMetric metric, final Random random) {
		return new MonteCarloCrossValidationEvaluator<>(dataset, this.getNumMCIterations(), this.getTrainFoldSize(), random, metric);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I, D> getSelf() {
		return this;
	}
}
