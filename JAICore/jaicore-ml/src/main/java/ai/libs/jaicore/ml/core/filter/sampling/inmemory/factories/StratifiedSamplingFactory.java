package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.IStratiAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, StratifiedSampling<I, D>> {

	private IStratiAmountSelector<I, D> stratiAmountSelector;
	private IStratiAssigner<I, D> stratiAssigner;
	private StratifiedSampling<I, D> previousRun = null;

	public StratifiedSamplingFactory(final IStratiAmountSelector<I, D> stratiAmountSelector, final IStratiAssigner<I, D> stratiAssigner) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
	}

	@Override
	public void setPreviousRun(final StratifiedSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public StratifiedSampling<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		StratifiedSampling<I, D> stratifiedSampling = new StratifiedSampling<I, D>(this.stratiAmountSelector, this.stratiAssigner, random, inputDataset);

		stratifiedSampling.setSampleSize(sampleSize);
		if (this.previousRun != null && this.previousRun.getStrati() != null) {
			stratifiedSampling.setStrati(this.previousRun.getStrati());
		}
		return stratifiedSampling;
	}

}
