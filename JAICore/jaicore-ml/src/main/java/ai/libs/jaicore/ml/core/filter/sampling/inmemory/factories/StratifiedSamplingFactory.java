package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.IStratifier;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingFactory<D extends IDataset<?>> extends ASampleAlgorithmFactory<D, StratifiedSampling<D>> implements ISamplingAlgorithmFactory<D, StratifiedSampling<D>> {

	private IStratifier stratificationTechniqe;

	public StratifiedSamplingFactory(final IStratifier stratificationTechniqe) {
		this.stratificationTechniqe = stratificationTechniqe;
	}

	@Override
	public StratifiedSampling<D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		StratifiedSampling<D> stratifiedSampling = new StratifiedSampling<>(this.stratificationTechniqe, random, inputDataset);

		stratifiedSampling.setSampleSize(sampleSize);
		return stratifiedSampling;
	}

}
