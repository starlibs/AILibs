package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import weka.core.Instances;

public class MonteCarloCrossValidationEvaluatorFactory implements IClassifierEvaluatorFactory {

	private IDatasetSplitter datasetSplitter;
	private ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator;
	private int seed;
	private int numMCIterations;
	private Instances data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;

	public MonteCarloCrossValidationEvaluatorFactory() {
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

	public MonteCarloCrossValidationEvaluatorFactory withDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withSplitBasedEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator) {
		this.splitBasedEvaluator = splitBasedClassifierEvaluator;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withSeed(final int seed) {
		this.seed = seed;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withNumMCIterations(final int numMCIterations) {
		this.numMCIterations = numMCIterations;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withData(final Instances data) {
		this.data = data;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withTrainFoldSize(final double trainFoldSize) {
		this.trainFoldSize = trainFoldSize;
		return this;
	}

	public MonteCarloCrossValidationEvaluatorFactory withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
		return this;
	}

	@Override
	public MonteCarloCrossValidationEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) {
		if (this.splitBasedEvaluator == null) {
			throw new IllegalStateException("Cannot create MCCV, because no splitBasedEvaluator has been set!");
		}
		return new MonteCarloCrossValidationEvaluator(this.splitBasedEvaluator, this.numMCIterations, dataset, this.trainFoldSize, seed);
	}
}
