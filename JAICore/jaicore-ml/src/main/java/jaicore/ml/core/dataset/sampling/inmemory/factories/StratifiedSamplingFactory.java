package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IOrderedDataset;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingFactory<I, D extends IOrderedDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, StratifiedSampling<I, D>> {

	private IStratiAmountSelector<D> stratiAmountSelector;
	private IStratiAssigner<I, D> stratiAssigner;
	private StratifiedSampling<I, D> previousRun = null;

	public StratifiedSamplingFactory(IStratiAmountSelector<D> stratiAmountSelector, IStratiAssigner<I, D> stratiAssigner) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
	}

	@Override
	public void setPreviousRun(StratifiedSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public StratifiedSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		StratifiedSampling<I, D> stratifiedSampling = new StratifiedSampling<>(this.stratiAmountSelector, this.stratiAssigner, random, inputDataset);

		stratifiedSampling.setSampleSize(sampleSize);
		if (previousRun != null && previousRun.getStrati() != null) {
			stratifiedSampling.setStrati(previousRun.getStrati());
		}
		return stratifiedSampling;
	}

}
