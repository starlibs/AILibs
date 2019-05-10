package jaicore.ml.core.dataset.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSplit<I extends IInstance> {

	private final IDataset<I> dataset;

	private IDataset<I> trainingData;

	private IDataset<I> testData;

	private final long seed;

	public StratifiedSplit(IDataset<I> dataset, long seed) {
		super();
		this.dataset = dataset;
		this.seed = seed;
	}

	public void doSplit(double trainPortion) throws AlgorithmException {
		Random r = new Random(seed);
		List<Integer> attributeIndices = Collections.singletonList(dataset.getNumberOfAttributes());
		AttributeBasedStratiAmountSelectorAndAssigner<I> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);
		StratifiedSampling<I> stratifiedSampling = new StratifiedSampling<>(selectorAndAssigner, selectorAndAssigner, r, dataset);
		int sampleSize = (int) (trainPortion * dataset.size());
		stratifiedSampling.setSampleSize(sampleSize);
		try {
			this.trainingData = stratifiedSampling.call();
			this.testData = dataset.createEmpty();
			testData.addAll(dataset);
			testData.removeAll(trainingData);
		} catch (AlgorithmExecutionCanceledException e) {
			throw new AlgorithmException("Stratified split has been cancelled");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public IDataset<I> getTrainingData() {
		return trainingData;
	}

	public IDataset<I> getTestData() {
		return testData;
	}

}
