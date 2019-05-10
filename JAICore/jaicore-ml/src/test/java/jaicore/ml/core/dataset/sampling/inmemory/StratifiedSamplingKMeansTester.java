package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.KMeansStratiAssigner;

public class StratifiedSamplingKMeansTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		@SuppressWarnings("unchecked")
		IDataset<I> dataset = (IDataset<I>) problem;
		KMeansStratiAssigner<I> k = new KMeansStratiAssigner<I>(new ManhattanDistance(), RANDOM_SEED);
		StratifiedSamplingFactory<I> factory = new StratifiedSamplingFactory<>(new IStratiAmountSelector<I>() {
			@Override
			public void setNumCPUs(int numberOfCPUs) {
			}

			@Override
			public int selectStratiAmount(IDataset<I> dataset) {
				return dataset.getNumberOfAttributes() * 2;
			}

			@Override
			public int getNumCPUs() {
				return 0;
			}
		}, k);
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * (double) dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
