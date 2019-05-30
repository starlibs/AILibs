package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IOrderedDataset;
import jaicore.ml.core.dataset.sampling.inmemory.SimpleRandomSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class SimpleRandomSamplingFactory<I, D extends IOrderedDataset<I>> implements ISamplingAlgorithmFactory<D, SimpleRandomSampling<I, D>> {

	@Override
	public SimpleRandomSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		SimpleRandomSampling<I, D> simpleRandomSampling = new SimpleRandomSampling<>(random, inputDataset);
		simpleRandomSampling.setSampleSize(sampleSize);
		return simpleRandomSampling;
	}

}
