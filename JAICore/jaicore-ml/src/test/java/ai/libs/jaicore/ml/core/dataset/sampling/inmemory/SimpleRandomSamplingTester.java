package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.dataset.sampling.IClusterableInstances;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;

public class SimpleRandomSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final IOrderedLabeledAttributeArrayDataset<IClusterableInstances<Object>, Object> dataset) {
		SimpleRandomSamplingFactory<IClusterableInstances<Object>, IOrderedLabeledAttributeArrayDataset<IClusterableInstances<Object>, Object>> factory = new SimpleRandomSamplingFactory<>();
		if (dataset != null) {
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}

}
