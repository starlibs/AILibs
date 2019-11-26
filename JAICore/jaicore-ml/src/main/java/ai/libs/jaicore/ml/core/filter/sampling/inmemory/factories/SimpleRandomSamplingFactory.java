package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SimpleRandomSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class SimpleRandomSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements ISamplingAlgorithmFactory<D, SimpleRandomSampling<D>> {

	@Override
	public SimpleRandomSampling<D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		SimpleRandomSampling<D> simpleRandomSampling = new SimpleRandomSampling<>(random, inputDataset);
		simpleRandomSampling.setSampleSize(sampleSize);
		return simpleRandomSampling;
	}

}
