package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.StratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.GMeansStratiAmountSelectorAndAssigner;

public class StratifiedSamplingGMeansTester extends GeneralSamplingTester<Object> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<IClusterableInstance> dataset) {
		GMeansStratiAmountSelectorAndAssigner g = new GMeansStratiAmountSelectorAndAssigner(RANDOM_SEED);
		StratifiedSamplingFactory<ILabeledDataset<?>> factory = new StratifiedSamplingFactory<>(g, g);
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
