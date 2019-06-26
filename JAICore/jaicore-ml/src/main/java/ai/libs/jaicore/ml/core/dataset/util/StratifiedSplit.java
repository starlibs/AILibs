package ai.libs.jaicore.ml.core.dataset.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.ml.core.dataset.DatasetCreationException;
import ai.libs.jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import ai.libs.jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSplit<I extends INumericLabeledAttributeArrayInstance<L>, L, D extends IOrderedLabeledAttributeArrayDataset<I, L>> {

	private final D dataset;

	private D trainingData;

	private D testData;

	private final long seed;

	public StratifiedSplit(final D dataset, final long seed) {
		super();
		this.dataset = dataset;
		this.seed = seed;
	}

	@SuppressWarnings("unchecked")
	public void doSplit(final double trainPortion) throws AlgorithmException {
		Random r = new Random(this.seed);
		List<Integer> attributeIndices = Collections.singletonList(this.dataset.getNumberOfAttributes());
		AttributeBasedStratiAmountSelectorAndAssigner<I, D> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);
		StratifiedSampling<I, D> stratifiedSampling = new StratifiedSampling<>(selectorAndAssigner, selectorAndAssigner, r, this.dataset);
		int sampleSize = (int) (trainPortion * this.dataset.size());
		stratifiedSampling.setSampleSize(sampleSize);
		try {
			this.trainingData = stratifiedSampling.call();
			this.testData = (D)this.dataset.createEmpty();
			this.testData.addAll(this.dataset);
			this.testData.removeAll(this.trainingData);
		} catch (AlgorithmExecutionCanceledException | AlgorithmTimeoutedException e) {
			throw new AlgorithmException("Stratified split has been cancelled");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (DatasetCreationException e) {
			throw new AlgorithmException("Could not create an empty copy of the given dataset.");
		}
	}

	public D getTrainingData() {
		return this.trainingData;
	}

	public D getTestData() {
		return this.testData;
	}

}
