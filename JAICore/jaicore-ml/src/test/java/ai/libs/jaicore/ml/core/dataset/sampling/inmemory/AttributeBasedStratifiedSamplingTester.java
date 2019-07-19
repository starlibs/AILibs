package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.dataset.sampling.IClusterableInstances;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;

public class AttributeBasedStratifiedSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final IOrderedLabeledAttributeArrayDataset<IClusterableInstances<Object>, Object> dataset) {

		List<Integer> attributeIndices = new ArrayList<>();
		// We assume that the target is the last attribute
		attributeIndices.add(dataset.getNumberOfAttributes());

		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstances<Object>, IOrderedLabeledAttributeArrayDataset<IClusterableInstances<Object>, Object>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(
				attributeIndices, DiscretizationStrategy.EQUAL_SIZE, 10);

		StratifiedSamplingFactory<IClusterableInstances<Object>, IOrderedLabeledAttributeArrayDataset<IClusterableInstances<Object>, Object>> factory = new StratifiedSamplingFactory<>(selectorAndAssigner, selectorAndAssigner);
		int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
		return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));

	}

}
