package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import ai.libs.jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.StratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.KMeansStratiAssigner;

public class StratifiedSamplingKMeansTester extends GeneralSamplingTester<Object> {

	private static final int RANDOM_SEED = 1;

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object> dataset) {
		KMeansStratiAssigner<INumericLabeledAttributeArrayInstance<Object>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>> k = new KMeansStratiAssigner<>(new ManhattanDistance(), RANDOM_SEED);
		StratifiedSamplingFactory<INumericLabeledAttributeArrayInstance<Object>, IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>> factory = new StratifiedSamplingFactory<>(
				new IStratiAmountSelector<IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object>>() {

					@Override
					public void setNumCPUs(final int numberOfCPUs) {
					}

					@Override
					public int selectStratiAmount(final IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<Object>, Object> dataset) {
						return dataset.getNumberOfAttributes() * 2;
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
