package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public abstract class ASampleAlgorithmFactory<D extends IDataset<?>, A extends ASamplingAlgorithm<D>> implements ISamplingAlgorithmFactory<D, A>, IReconstructible {
	private int sampleSize;
	private long seed;
	private Random random;

	public static <D extends IDataset<?>, A extends ASamplingAlgorithm<D>, T extends ASampleAlgorithmFactory<D, A>> T create(final Class<T> factoryClazz, final int sampleSize, final long seed) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		T factory = factoryClazz.getConstructor().newInstance();
		factory.setRandom(new Random(seed));
		factory.setSampleSize(sampleSize);
		return factory;
	}

	protected ReconstructionInstruction getConstructionInstruction() {
		return new ReconstructionInstruction(ASampleAlgorithmFactory.class.getName(), "create", new Class<?>[] {Class.class, int.class, long.class}, new Object[] {this.getClass(), this.sampleSize, this.seed});
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		return new ReconstructionPlan(Arrays.asList(this.getConstructionInstruction()));
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		throw new UnsupportedOperationException("No instructions can be added to a sampling factory.");
	}

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
		this.seed = -1;
	}

	public long getSeed() {
		return this.seed;
	}

	public void setSeed(final long seed) {
		this.seed = seed;
		this.random = new Random(seed);
	}
}
