package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.SimpleRandomSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class SimpleRandomSamplingFactory<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> implements ISamplingAlgorithmFactory<X, Y, I, D, SimpleRandomSampling<X, Y, I, D>> {

	@Override
	public SimpleRandomSampling<X, Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		SimpleRandomSampling<X, Y, I, D> simpleRandomSampling = new SimpleRandomSampling<>(random, inputDataset);
		simpleRandomSampling.setSampleSize(sampleSize);
		return simpleRandomSampling;
	}

}
