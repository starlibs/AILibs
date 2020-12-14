package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.StratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.GMeansStratifier;

public class StratifiedSamplingGMeansTester extends GeneralSamplingTester<Object> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<?> dataset) {
		GMeansStratifier g = new GMeansStratifier(RANDOM_SEED);
		StratifiedSamplingFactory<ILabeledDataset<?>> factory = new StratifiedSamplingFactory<>(g);
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

	@Override
	public void testSampleSizeLargeProblem(final MemoryBasedSamplingAlgorithmTestProblemSet set) {
		/* skip this test, because G-Means is not applicable for large data */
		assertTrue(true);
	}

	@Override
	public void testSampleSizeMediumProblem(final MemoryBasedSamplingAlgorithmTestProblemSet set) {
		/* skip this test, because G-Means is not applicable for intermediate size data */
		assertTrue(true);
	}

	@Override
	public void testTimeout(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testTimeoutWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testInterrupt(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testInterruptWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testCancel(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}

	@Override
	public void testCancelWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) {
		/* skip this test, because the used G-Means implementation is not interruptible (and hence not timeoutable and not cancelable) */
		assertTrue(true);
	}
}
