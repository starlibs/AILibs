package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.GmeansSamplingFactory;

public class GMeansSamplingTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static long SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public void testSampleSizeLargeProblem() throws Exception {
		// Sample Size is not supported for GMeansSampling
	}

	@Override
	public void testSampleSizeSmallProblem() throws Exception {
		// Sample Size is not supported for GMeansSampling
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		@SuppressWarnings("unchecked")
		IDataset<I> dataset = (IDataset<I>) problem;
		GmeansSamplingFactory<I> factory = new GmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(SEED));
		}
		return null;
	}
}
