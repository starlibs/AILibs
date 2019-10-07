package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation.ISplitBasedClassifierEvaluator;
import weka.core.Instances;

/**
 * Factory for configuring standard Monte Carlo cross-validation evaluators.
 * @author mwever
 *
 */
public class MonteCarloCrossValidationEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends AMonteCarloCrossValidationBasedEvaluatorFactory<I, D> {

	/**
	 * Standard C'tor.
	 */
	public MonteCarloCrossValidationEvaluatorFactory<I,D>() {
		super();
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withDatasetSplitter(final IDatasetSplitter<I,D> datasetSplitter) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withDatasetSplitter(datasetSplitter);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withSplitBasedEvaluator(splitBasedClassifierEvaluator);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withSeed(final int seed) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withSeed(seed);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withNumMCIterations(final int numMCIterations) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withNumMCIterations(numMCIterations);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withData(final Instances data) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withData(data);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withTrainFoldSize(final double trainFoldSize) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withTrainFoldSize(trainFoldSize);
	}

	@Override
	public MonteCarloCrossValidationEvaluatorFactory<I,D> withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		return (MonteCarloCrossValidationEvaluatorFactory<I,D>) super.withTimeoutForSolutionEvaluation(timeoutForSolutionEvaluation);
	}

	@Override
	public MonteCarloCrossValidationEvaluator<I,D> getIClassifierEvaluator(final D dataset, final long seed) {
		if (this.getSplitBasedEvaluator() == null) {
			throw new IllegalStateException("Cannot create MCCV, because no splitBasedEvaluator has been set!");
		}
		return new MonteCarloCrossValidationEvaluator<>(this.getSplitBasedEvaluator(), this.getDatasetSplitter(), this.getNumMCIterations(), dataset, this.getTrainFoldSize(), seed);
	}
}
