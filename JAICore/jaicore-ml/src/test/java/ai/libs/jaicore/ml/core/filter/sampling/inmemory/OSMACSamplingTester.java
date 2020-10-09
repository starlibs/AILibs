package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.OSMACSamplingFactory;

public class OSMACSamplingTester extends GeneralSamplingTester<Object> {

	private static final long RANDOM_SEED = 1;
	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	private static final double PRE_SAMPLING_FRACTION = 0.01;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final ILabeledDataset<?> dataset) {
		OSMACSamplingFactory factory = new OSMACSamplingFactory();
		if (dataset != null && dataset.getLabelAttribute() instanceof ICategoricalAttribute) {
			factory.setPreSampleSize((int) (PRE_SAMPLING_FRACTION * dataset.size()));
			int sampleSize = (int) (DEFAULT_SAMPLE_FRACTION * dataset.size());
			factory.setPilot(new MajorityClassifier());
			factory.setRandom(new Random(RANDOM_SEED));
			return factory.getAlgorithm(sampleSize, dataset, new Random(RANDOM_SEED));
		}
		return null;
	}
}
