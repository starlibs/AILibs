package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.SimpleRandomSampling;

public class SimpleRandomSamplingFactory<I extends IInstance> implements ISamplingAlgorithmFactory<I> {

	private int sampleSize = -1;
	private IDataset<I> inputDataset = null;
	private Random random = new Random();

	@Override
	public void setSampleSize(int sameplSize) {
		this.sampleSize = sameplSize;
	}

	@Override
	public void setInputDataset(IDataset<I> inputDataset) {
		this.inputDataset = inputDataset;
	}

	@Override
	public void setRandom(long seed) {
		this.random.setSeed(seed);
	}

	@Override
	public void setRandom(Random random) {
		this.random = random;
	}

	@Override
	public ASamplingAlgorithm<I> getAlgorithm() throws IllegalStateException {
		if (this.sampleSize == -1) {
			throw new IllegalStateException(
					"Invalid sample size! A size that the sample should have must be specified before creating the sampling algorithm.");
		}
		if (this.inputDataset == null) {
			throw new IllegalStateException(
					"No input given! A input dataset where the sample should be drawn from must be specified before creating the sampling algorithm.");
		}
		SimpleRandomSampling<I> simpleRandomSampling = new SimpleRandomSampling<>(this.random);
		simpleRandomSampling.setSampleSize(this.sampleSize);
		simpleRandomSampling.setInput(this.inputDataset);
		return simpleRandomSampling;
	}

}
