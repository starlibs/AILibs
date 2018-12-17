package jaicore.ml.core.dataset.sampling;

import java.util.Random;

import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.sampling.stratified.sampling.IStratiAmountSelector;
import jaicore.ml.core.dataset.sampling.stratified.sampling.KMeansStratiAssigner;
import jaicore.ml.core.dataset.sampling.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingKMeansTester extends GeneralSamplingTester {

	private static final int RANDOM_SEED = 1;

	@Override
	public IAlgorithmFactory<IDataset, IDataset> getFactory() {
		return new IAlgorithmFactory<IDataset, IDataset>() {

			private IDataset input;

			@Override
			public void setProblemInput(IDataset problemInput) {
				this.input = problemInput;
			}

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, IDataset> reducer) {
				throw new UnsupportedOperationException("Problem input not applicable for subsampling algorithms!");
			}

			@Override
			public IAlgorithm<IDataset, IDataset> getAlgorithm() {
				KMeansStratiAssigner k = new KMeansStratiAssigner(new ManhattanDistance(), RANDOM_SEED);
				AAlgorithm<IDataset, IDataset> algorithm = new StratifiedSampling(new IStratiAmountSelector() {
					@Override
					public void setNumCPUs(int numberOfCPUs) {
					}
					@Override
					public int selectStratiAmount(IDataset dataset) {
						return dataset.getNumberOfAttributes() * 2;
					}
					@Override
					public int getNumCPUs() {
						return 0;
					}
				}, k, new Random(RANDOM_SEED), true);
				if (this.input != null) {
					algorithm.setInput(input);
				}
				return algorithm;
			}
		};
	}
	
}
