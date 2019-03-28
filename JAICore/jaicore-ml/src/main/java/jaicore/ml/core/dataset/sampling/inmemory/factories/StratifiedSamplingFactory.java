package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingFactory<I extends IInstance>
		implements IRerunnableSamplingAlgorithmFactory<I, StratifiedSampling<I>> {

	private IStratiAmountSelector<I> stratiAmountSelector;
	private IStratiAssigner<I> stratiAssigner;
	private StratifiedSampling<I> previousRun = null;

	public StratifiedSamplingFactory(IStratiAmountSelector<I> stratiAmountSelector, IStratiAssigner<I> stratiAssigner) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
	}

	@Override
	public void setPreviousRun(StratifiedSampling<I> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public StratifiedSampling<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		StratifiedSampling<I> stratifiedSampling = new StratifiedSampling<>(this.stratiAmountSelector,
				this.stratiAssigner, random, inputDataset);

		stratifiedSampling.setSampleSize(sampleSize);
		if (previousRun != null && previousRun.getStrati() != null) {
			stratifiedSampling.setStrati(previousRun.getStrati());
		}
		return stratifiedSampling;
	}

}
