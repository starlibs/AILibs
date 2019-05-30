package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;

public class AttributeBasedStratifiedSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object> dataset) {

		List<Integer> attributeIndices = new ArrayList<>();
		// We assume that the target is the last attribute
		attributeIndices.add(dataset.getNumberOfAttributes());

		AttributeBasedStratiAmountSelectorAndAssigner<INumericLabeledAttributeArrayInstance<Object>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(
				attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);

		StratifiedSamplingFactory<INumericLabeledAttributeArrayInstance<Object>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>> factory = new StratifiedSamplingFactory<>(selectorAndAssigner, selectorAndAssigner);
		int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
		return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));

	}

}
