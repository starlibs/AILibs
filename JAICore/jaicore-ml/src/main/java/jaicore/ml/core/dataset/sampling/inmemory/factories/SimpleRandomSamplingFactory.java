package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.SimpleRandomSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class SimpleRandomSamplingFactory<I extends IInstance>
		implements ISamplingAlgorithmFactory<I, SimpleRandomSampling<I>> {

	@Override
	public SimpleRandomSampling<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		SimpleRandomSampling<I> simpleRandomSampling = new SimpleRandomSampling<>(random, inputDataset);
		simpleRandomSampling.setSampleSize(sampleSize);
		return simpleRandomSampling;
	}

}
