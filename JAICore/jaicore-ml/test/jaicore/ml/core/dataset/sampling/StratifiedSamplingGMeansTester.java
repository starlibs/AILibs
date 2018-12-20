package jaicore.ml.core.dataset.sampling;

import java.util.Random;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.sampling.stratified.sampling.GMeansStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingGMeansTester extends GeneralSamplingTester {

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
				GMeansStratiAmountSelectorAndAssigner g = new GMeansStratiAmountSelectorAndAssigner(RANDOM_SEED);
				AAlgorithm<IDataset, IDataset> algorithm = new StratifiedSampling(g, g, new Random(RANDOM_SEED));
				if (this.input != null) {
					algorithm.setInput(input);
				}
				return algorithm;
			}
		};
	}
	
}
