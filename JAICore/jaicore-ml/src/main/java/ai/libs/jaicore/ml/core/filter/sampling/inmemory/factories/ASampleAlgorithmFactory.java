package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public abstract class ASampleAlgorithmFactory<D extends IDataset<?>, A extends ASamplingAlgorithm<D>> implements ISamplingAlgorithmFactory<D, A> {
	private int sampleSize;
	private Random random;

	@Override
	public A getAlgorithm(final D inputDataset) {
		Objects.requireNonNull(this.random);
		if (this.sampleSize == 0 || this.sampleSize > inputDataset.size()) {
			throw new IllegalStateException("Illegal sample size " + this.sampleSize + " for dataset with " + inputDataset.size() + " points.");
		}
		return this.getAlgorithm(this.sampleSize, inputDataset, this.random);
	}

	public int getSampleSize() {
		return this.sampleSize;
	}

	public void setSampleSize(final int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public Random getRandom() {
		return this.random;
	}

	public void setRandom(final Random random) {
		this.random = random;
	}
}
