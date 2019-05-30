package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.factories.OSMACSamplingFactory;

public class OSMACSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	private static final double PRE_SAMPLING_FRACTION = 0.01;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object> dataset) {
		OSMACSamplingFactory<INumericLabeledAttributeArrayInstance<Object>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>> factory = new OSMACSamplingFactory<>();
		if (dataset != null) {
			factory.setPreSampleSize((int) (PRE_SAMPLING_FRACTION * dataset.size()));
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}
}
