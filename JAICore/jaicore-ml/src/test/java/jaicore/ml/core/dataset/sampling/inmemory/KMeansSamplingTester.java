package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
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
	public IAlgorithmFactory<IDataset<I>, IDataset<I>> getFactory() {
		return new IAlgorithmFactory<IDataset<I>, IDataset<I>>() {

			private IDataset<I> input;

			@Override
			public void setProblemInput(IDataset<I> problemInput) {
				this.input = problemInput;
			}

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, IDataset<I>> reducer) {
				throw new UnsupportedOperationException("Problem input not applicable for subsampling algorithms!");
			}

			@Override
			public IAlgorithm<IDataset<I>, IDataset<I>> getAlgorithm() {
				KmeansSamplingFactory<I> factory = new KmeansSamplingFactory<>();
				if (this.input != null) {
					factory.setClusterSeed(SEED);
					factory.setK(K);
					int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) input.size());
					return factory.getAlgorithm(sampleSize, input, new Random(SEED));
				}
				return null;
			}
		};
	}
}
