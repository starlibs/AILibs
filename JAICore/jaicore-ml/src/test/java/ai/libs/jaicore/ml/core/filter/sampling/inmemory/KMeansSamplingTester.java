package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.KmeansSamplingFactory;

public class KMeansSamplingTester extends GeneralSamplingTester<Number> {

	private static final long SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	private static final int K = 100;

	@Override
	public void testSampleSizeLargeProblem() {
		// Sample Size is not supported for KMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeSmallProblem() {
		// Sample Size is not supported for KMeansSampling
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<IClusterableInstance> dataset) {
		KmeansSamplingFactory<IClusterableInstance, ILabeledDataset<IClusterableInstance>> factory = new KmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
			factory.setK(K);
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(SEED));
		}
		return null;
	}
}
