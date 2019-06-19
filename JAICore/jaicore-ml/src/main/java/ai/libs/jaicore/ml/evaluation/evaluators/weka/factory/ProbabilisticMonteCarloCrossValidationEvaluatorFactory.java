package ai.libs.jaicore.ml.evaluation.evaluators.weka.factory;

import ai.libs.jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import weka.core.Instances;

/**
 * Factory for configuring probabilistic Monte Carlo cross-validation evaluators.
 *
 * @author mwever
 *
 */
public class ProbabilisticMonteCarloCrossValidationEvaluatorFactory extends AMonteCarloCrossValidationBasedEvaluatorFactory {

	/**
	 * Standard c'tor.
	 */
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory() {
		super();
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withDatasetSplitter(datasetSplitter);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withSplitBasedEvaluator(splitBasedClassifierEvaluator);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withSeed(final int seed) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withSeed(seed);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withNumMCIterations(final int numMCIterations) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withNumMCIterations(numMCIterations);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withData(final Instances data) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withData(data);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withTrainFoldSize(final double trainFoldSize) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withTrainFoldSize(trainFoldSize);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory) super.withTimeoutForSolutionEvaluation(timeoutForSolutionEvaluation);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) {
		if (this.getSplitBasedEvaluator() == null) {
			throw new IllegalStateException("Cannot create MCCV, because no splitBasedEvaluator has been set!");
		}
		return new ProbabilisticMonteCarloCrossValidationEvaluator(this.getSplitBasedEvaluator(), this.getDatasetSplitter(), this.getNumMCIterations(), 1, dataset, this.getTrainFoldSize(), seed);
	}
}
