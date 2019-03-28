package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.GMeansStratiAmountSelectorAndAssigner;

public class StratifiedSamplingGMeansTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		@SuppressWarnings("unchecked")
		IDataset<I> dataset = (IDataset<I>) problem;
		GMeansStratiAmountSelectorAndAssigner<I> g = new GMeansStratiAmountSelectorAndAssigner<I>(RANDOM_SEED);
		StratifiedSamplingFactory<I> factory = new StratifiedSamplingFactory<>(g, g);
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
