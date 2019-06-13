package ai.libs.jaicore.ml.evaluation.evaluators.weka.factory;

import ai.libs.jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import weka.core.Instances;

public class ProbabilisticMonteCarloCrossValidationEvaluatorFactory implements IClassifierEvaluatorFactory {

	private IDatasetSplitter datasetSplitter;
	private ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator;
	private int seed;
	private int numMCIterations;
	private Instances data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory() {
		super();
	}

	public IDatasetSplitter getDatasetSplitter() {
		return this.datasetSplitter;
	}

	public ISplitBasedClassifierEvaluator<Double> getSplitBasedEvaluator() {
		return this.splitBasedEvaluator;
	}

	public int getSeed() {
		return this.seed;
	}

	public int getNumMCIterations() {
		return this.numMCIterations;
	}

	public Instances getData() {
		return this.data;
	}

	public double getTrainFoldSize() {
		return this.trainFoldSize;
	}

	public int getTimeoutForSolutionEvaluation() {
		return this.timeoutForSolutionEvaluation;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator) {
		this.splitBasedEvaluator = splitBasedClassifierEvaluator;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withSeed(final int seed) {
		this.seed = seed;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withNumMCIterations(final int numMCIterations) {
		this.numMCIterations = numMCIterations;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withData(final Instances data) {
		this.data = data;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withTrainFoldSize(final double trainFoldSize) {
		this.trainFoldSize = trainFoldSize;
		return this;
	}

	public ProbabilisticMonteCarloCrossValidationEvaluatorFactory withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
		return this;
	}

	@Override
	public ProbabilisticMonteCarloCrossValidationEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) {
		if (this.splitBasedEvaluator == null) {
			throw new IllegalStateException("Cannot create MCCV, because no splitBasedEvaluator has been set!");
		}
		return new ProbabilisticMonteCarloCrossValidationEvaluator(this.splitBasedEvaluator, this.datasetSplitter, this.numMCIterations, 1, dataset, this.trainFoldSize, seed);
	}
}
