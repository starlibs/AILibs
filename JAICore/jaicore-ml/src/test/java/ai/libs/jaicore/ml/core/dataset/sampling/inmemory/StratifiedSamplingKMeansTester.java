package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.StratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.KMeansStratiAssigner;

public class StratifiedSamplingKMeansTester extends GeneralSamplingTester<Object> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<IClusterableInstance> dataset) {
		KMeansStratiAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> k = new KMeansStratiAssigner<>(new ManhattanDistance(), RANDOM_SEED);
		StratifiedSamplingFactory<IClusterableInstance, ILabeledDataset<IClusterableInstance>> factory = new StratifiedSamplingFactory<>(new IStratiAmountSelector<IClusterableInstance, ILabeledDataset<IClusterableInstance>>() {
			@Override
			public void setNumCPUs(final int numberOfCPUs) {
			}

			@Override
			public int selectStratiAmount(final ILabeledDataset<IClusterableInstance> dataset) {
				return dataset.getNumAttributes() * 2;
			}

			@Override
			public int getNumCPUs() {
				return 0;
			}
		}, k);
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
