package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.KmeansSamplingFactory;

public class KMeansSamplingTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static long SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	private static int K = 100;

	@Override
	public void testSampleSizeLargeProblem() throws Exception {
		// Sample Size is not supported for KMeansSampling
	}

	@Override
	public void testSampleSizeSmallProblem() throws Exception {
		// Sample Size is not supported for KMeansSampling
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		@SuppressWarnings("unchecked")
		IDataset<I> dataset = (IDataset<I>) problem;
		KmeansSamplingFactory<I> factory = new KmeansSamplingFactory<>();
		if (dataset != null) {
			factory.setClusterSeed(SEED);
			factory.setK(K);
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(SEED));
		}
		return null;
	}
}
