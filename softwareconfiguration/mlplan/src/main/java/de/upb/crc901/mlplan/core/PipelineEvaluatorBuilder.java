package de.upb.crc901.mlplan.core;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.IEvaluatorMeasureBridge;
import jaicore.ml.wekautil.dataset.splitter.IDatasetSplitter;
import weka.core.Instances;

public class PipelineEvaluatorBuilder {

	private IClassifierFactory classifierFactory;
	private IDatasetSplitter datasetSplitter;
	private IEvaluatorMeasureBridge<Double> evaluationMeasurementBridge;
	private int seed;
	private int numMCIterations;
	private Instances data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;
	private IClassifierEvaluator classifierEvaluator;

	public PipelineEvaluatorBuilder() {
		super();
	}

	public IClassifierFactory getClassifierFactory() {
		return this.classifierFactory;
	}

	public IDatasetSplitter getDatasetSplitter() {
		return this.datasetSplitter;
	}

	public IEvaluatorMeasureBridge<Double> getEvaluationMeasurementBridge() {
		return this.evaluationMeasurementBridge;
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

	public PipelineEvaluatorBuilder withClassifierFactory(final IClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
		return this;
	}

	public PipelineEvaluatorBuilder withDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this;
	}

	public PipelineEvaluatorBuilder withEvaluationMeasurementBridge(final IEvaluatorMeasureBridge<Double> evaluationMeasurementBridge) {
		this.evaluationMeasurementBridge = evaluationMeasurementBridge;
		return this;
	}

	public PipelineEvaluatorBuilder withSeed(final int seed) {
		this.seed = seed;
		return this;
	}

	public PipelineEvaluatorBuilder withNumMCIterations(final int numMCIterations) {
		this.numMCIterations = numMCIterations;
		return this;
	}

	public PipelineEvaluatorBuilder withData(final Instances data) {
		this.data = data;
		return this;
	}

	public PipelineEvaluatorBuilder withTrainFoldSize(final double trainFoldSize) {
		this.trainFoldSize = trainFoldSize;
		return this;
	}

	public PipelineEvaluatorBuilder withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
		return this;
	}

	public void withClassifierEvaluator(final IClassifierEvaluator classifierEvaluator) {
		this.classifierEvaluator = classifierEvaluator;
	}

	public IClassifierEvaluator getClassifierEvaluator() {
		return this.classifierEvaluator;
	}

}
