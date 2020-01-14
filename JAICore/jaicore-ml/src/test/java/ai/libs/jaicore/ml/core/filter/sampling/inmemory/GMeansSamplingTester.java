package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.GmeansSamplingFactory;

public class GMeansSamplingTester extends GeneralSamplingTester<Number> {

	private static final long SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;


	@Override
	public void testSampleSizeSimpleProblem() {
		// Sample Size is not supported for GMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeMediumProblem() {
		// Sample Size is not supported for GMeansSampling
		assertTrue(true);
	}

	@Override
	public void testSampleSizeLargeProblem() {
		// Sample Size is not supported for GMeansSampling
		assertTrue(true);
	}

	@Override
	public void testNoDuplicatesSmallProblem() {
		// GMeansSampling cannot be used for too large problems, because it is too slow
		assertTrue(true);
	}

	@Override
	public void testNoDuplicatesLargeProblem() {
		// GMeansSampling cannot be used for too large problems, because it is too slow
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<?> dataset) {
		GmeansSamplingFactory<IClusterableInstance, ILabeledDataset<IClusterableInstance>> factory = new GmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
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
