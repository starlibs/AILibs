package jaicore.ml.core.dataset.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSplit<I extends INumericLabeledAttributeArrayInstance<L>, L, D extends IOrderedLabeledAttributeArrayDataset<I, L>> {

	private final D dataset;

	private D trainingData;

	private D testData;

	private final long seed;

	public StratifiedSplit(D dataset, long seed) {
		super();
		this.dataset = dataset;
		this.seed = seed;
	}

	@SuppressWarnings("unchecked")
	public void doSplit(double trainPortion) throws AlgorithmException {
		Random r = new Random(seed);
		List<Integer> attributeIndices = Collections.singletonList(dataset.getNumberOfAttributes());
		AttributeBasedStratiAmountSelectorAndAssigner<I, D> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);
		StratifiedSampling<I, D> stratifiedSampling = new StratifiedSampling<>(selectorAndAssigner, selectorAndAssigner, r, dataset);
		int sampleSize = (int) (trainPortion * dataset.size());
		stratifiedSampling.setSampleSize(sampleSize);
		try {
			this.trainingData = stratifiedSampling.call();
			this.testData = (D)dataset.createEmpty();
			testData.addAll(dataset);
			testData.removeAll(trainingData);
		} catch (AlgorithmExecutionCanceledException e) {
			throw new AlgorithmException("Stratified split has been cancelled");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (DatasetCreationException e) {
			throw new AlgorithmException("Could not create an empty copy of the given dataset.");
		}
	}

	public D getTrainingData() {
		return trainingData;
	}

	public D getTestData() {
		return testData;
	}

}
