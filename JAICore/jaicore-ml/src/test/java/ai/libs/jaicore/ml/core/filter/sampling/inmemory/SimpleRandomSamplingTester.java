package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.SimpleRandomSamplingFactory;

public class SimpleRandomSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<?> dataset) {
		SimpleRandomSamplingFactory<ILabeledDataset<?>> factory = new SimpleRandomSamplingFactory<>();
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
