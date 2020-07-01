package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Objects;

import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.core.evaluation.AveragingPredictionPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;

/**
 * Factory for configuring standard Monte Carlo cross-validation evaluators.
 * The basic performance measure is always averaged over the different runs.
 *
 * @author mwever
 * @author fmohr
 *
 */
public class MonteCarloCrossValidationEvaluatorFactory extends AMonteCarloCrossValidationBasedEvaluatorFactory<MonteCarloCrossValidationEvaluatorFactory> {

	@Override
	public MonteCarloCrossValidationEvaluator getLearnerEvaluator() {
		if (this.getTrainFoldSize() <= 0 || this.getTrainFoldSize() >= 1) {
			throw new IllegalStateException("Train fold size is configured to " + this.getTrainFoldSize() + " but must be strictly greater than 0 and strictly smaller than 1.");
		}
		Objects.requireNonNull(this.random);
		Objects.requireNonNull(this.data);
		Objects.requireNonNull(this.metric);
		if (this.numMCIterations <= 0) {
			throw new IllegalStateException("Cannot create MCCV evaluator due to invalid number of repeats " + this.getNumMCIterations() + ". Set number of repeats to a positive value!");
		}
		IAggregatedPredictionPerformanceMeasure<?, ?> aggMeasure = new AveragingPredictionPerformanceMeasure<>(this.metric);
		return new MonteCarloCrossValidationEvaluator(this.getCacheSplitSets(), this.data, this.getNumMCIterations(), this.getTrainFoldSize(), this.random, aggMeasure);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory getSelf() {
		return this;
	}
}
