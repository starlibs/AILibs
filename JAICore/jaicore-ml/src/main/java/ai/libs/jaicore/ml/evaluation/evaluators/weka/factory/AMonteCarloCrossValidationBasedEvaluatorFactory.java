package ai.libs.jaicore.ml.evaluation.evaluators.weka.factory;

import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import weka.core.Instances;

public abstract class AMonteCarloCrossValidationBasedEvaluatorFactory implements IClassifierEvaluatorFactory {

	private IDatasetSplitter datasetSplitter;
	private ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator;
	private int seed;
	private int numMCIterations;
	private Instances data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;

	protected AMonteCarloCrossValidationBasedEvaluatorFactory() {
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

	public AMonteCarloCrossValidationBasedEvaluatorFactory withDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator) {
		this.splitBasedEvaluator = splitBasedClassifierEvaluator;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withSeed(final int seed) {
		this.seed = seed;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withNumMCIterations(final int numMCIterations) {
		this.numMCIterations = numMCIterations;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withData(final Instances data) {
		this.data = data;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withTrainFoldSize(final double trainFoldSize) {
		this.trainFoldSize = trainFoldSize;
		return this;
	}

	public AMonteCarloCrossValidationBasedEvaluatorFactory withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
		return this;
	}
}
