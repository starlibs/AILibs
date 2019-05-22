package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;

public class SimpleRandomSamplingTester extends GeneralSamplingTester {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance> dataset) {
		SimpleRandomSamplingFactory<INumericLabeledAttributeArrayInstance, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance>> factory = new SimpleRandomSamplingFactory<>();
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
