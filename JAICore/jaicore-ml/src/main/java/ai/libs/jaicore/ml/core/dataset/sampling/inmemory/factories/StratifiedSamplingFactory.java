package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAmountSelector;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.IStratiAssigner;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSamplingFactory<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> implements IRerunnableSamplingAlgorithmFactory<X, Y, I, D, StratifiedSampling<X, Y, I, D>> {

	private IStratiAmountSelector<X, Y, I, D> stratiAmountSelector;
	private IStratiAssigner<X, Y, I, D> stratiAssigner;
	private StratifiedSampling<X, Y, I, D> previousRun = null;

	public StratifiedSamplingFactory(final IStratiAmountSelector<X, Y, I, D> stratiAmountSelector, final IStratiAssigner<X, Y, I, D> stratiAssigner) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
	}

	@Override
	public void setPreviousRun(final StratifiedSampling<X, Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public StratifiedSampling<X, Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		StratifiedSampling<X, Y, I, D> stratifiedSampling = new StratifiedSampling<X, Y, I, D>(this.stratiAmountSelector, this.stratiAssigner, random, inputDataset);

		stratifiedSampling.setSampleSize(sampleSize);
		if (this.previousRun != null && this.previousRun.getStrati() != null) {
			stratifiedSampling.setStrati(this.previousRun.getStrati());
		}
		return stratifiedSampling;
	}

}
