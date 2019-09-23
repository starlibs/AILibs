package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.evaluation.evaluator.ProbabilisticMonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation.ISplitBasedClassifierEvaluator;
import weka.core.Instances;

/**
 * Factory for configuring probabilistic Monte Carlo cross-validation evaluators.
 *
 * @author mwever
 *
 */
public class ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends AMonteCarloCrossValidationBasedEvaluatorFactory<I, D> {

	/**
	 * Standard c'tor.
	 */
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory() {
		super();
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withDatasetSplitter(final IDatasetSplitter<I, D> datasetSplitter) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withDatasetSplitter(datasetSplitter);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double, I, D> splitBasedClassifierEvaluator) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withSplitBasedEvaluator(splitBasedClassifierEvaluator);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withSeed(final int seed) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withSeed(seed);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withNumMCIterations(final int numMCIterations) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withNumMCIterations(numMCIterations);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withData(final Instances data) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withData(data);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withTrainFoldSize(final double trainFoldSize) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withTrainFoldSize(trainFoldSize);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D> withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		return (ProbabilisticMonteCarloCrossValidationEvaluatorFactory<I, D>) super.withTimeoutForSolutionEvaluation(timeoutForSolutionEvaluation);
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluator<I, D> getIClassifierEvaluator(final D dataset, final long seed) {
		if (this.getSplitBasedEvaluator() == null) {
			throw new IllegalStateException("Cannot create MCCV, because no splitBasedEvaluator has been set!");
		}
		return new ProbabilisticMonteCarloCrossValidationEvaluator<I, D>(this.getSplitBasedEvaluator(), this.getDatasetSplitter(), this.getNumMCIterations(), 1, dataset, this.getTrainFoldSize(), seed);
	}
}
