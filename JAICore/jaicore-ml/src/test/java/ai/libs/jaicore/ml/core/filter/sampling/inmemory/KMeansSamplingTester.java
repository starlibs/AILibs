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
	public void testSampleSizeSimpleProblem() {
		// Sample Size is not supported for KMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeMediumProblem() {
		// Sample Size is not supported for KMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeLargeProblem() {
		// Sample Size is not supported for KMeansSampling
		assertTrue(true);
	}

	@Override
	public void testNoDuplicatesLargeProblem() {
		// KMeansSampling cannot be used for too large problems, because it is too slow
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<?> dataset) {
		KmeansSamplingFactory<IClusterableInstance, ILabeledDataset<IClusterableInstance>> factory = new KmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
			factory.setK(K);
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, (ILabeledDataset<IClusterableInstance>)dataset, new Random(SEED));
		}
		return null;
	}

	@Override
	public void testTimeout() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testTimeoutWhenParallelized() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testInterrupt() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testInterruptWhenParallelized() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testCancel() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testCancelWhenParallelized() {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}
}
