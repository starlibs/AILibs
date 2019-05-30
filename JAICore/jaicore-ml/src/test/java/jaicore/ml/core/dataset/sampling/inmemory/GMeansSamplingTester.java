package jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.factories.GmeansSamplingFactory;

public class GMeansSamplingTester extends GeneralSamplingTester<Number> {

	private static final long SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public void testSampleSizeLargeProblem() {
		// Sample Size is not supported for GMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeSmallProblem() {
		// Sample Size is not supported for GMeansSampling
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Number>, Number> dataset) {
		GmeansSamplingFactory<INumericLabeledAttributeArrayInstance<Number>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Number>, Number>> factory = new GmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(SEED));
		}
		return null;
	}
}
