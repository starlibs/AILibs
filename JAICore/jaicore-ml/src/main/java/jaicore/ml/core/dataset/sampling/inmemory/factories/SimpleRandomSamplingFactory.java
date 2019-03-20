package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.SimpleRandomSampling;

public class SimpleRandomSamplingFactory<I extends IInstance> implements ISamplingAlgorithmFactory<I> {

	@Override
	public ASamplingAlgorithm<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		SimpleRandomSampling<I> simpleRandomSampling = new SimpleRandomSampling<>(random);
		simpleRandomSampling.setSampleSize(sampleSize);
		simpleRandomSampling.setInput(inputDataset);
		return simpleRandomSampling;
	}

}
