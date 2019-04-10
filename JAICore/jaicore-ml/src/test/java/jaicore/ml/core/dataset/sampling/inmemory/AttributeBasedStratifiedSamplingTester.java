package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;

public class AttributeBasedStratifiedSamplingTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		@SuppressWarnings("unchecked")
		IDataset<I> dataset = (IDataset<I>) problem;

		List<Integer> attributeIndices = new ArrayList<>();
		// We assume that the target is the last attribute
		attributeIndices.add(dataset.getNumberOfAttributes());

		AttributeBasedStratiAmountSelectorAndAssigner<I> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(
				attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);

		StratifiedSamplingFactory<I> factory = new StratifiedSamplingFactory<>(selectorAndAssigner,
				selectorAndAssigner);
		int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
		return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));

	}

}
