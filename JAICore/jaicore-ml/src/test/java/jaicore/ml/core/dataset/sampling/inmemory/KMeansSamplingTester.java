package jaicore.ml.core.dataset.sampling.inmemory;

import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.KmeansSampling;

public class KMeansSamplingTester<I extends IInstance> extends GeneralSamplingTester<I> {
	
	private static long SEED = 1;
	private static double SAMLPING_FRACTION = 1;
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
				ASamplingAlgorithm<I> algorithm;
				if (this.input != null) {
					algorithm = new KmeansSampling<>(SEED, K, new ManhattanDistance());
					algorithm.setInput(input);
					int sampleSize = (int) (SAMLPING_FRACTION * (double) input.size());
					algorithm.setSampleSize(sampleSize);
					return algorithm;
				}
				else {
					throw new NullPointerException("Input is not allowed to be null");
				}
			}
		};
	}
}
