package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.LabelBasedStratifiedSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class LabelBasedStratifiedSamplingFactory<D extends ILabeledDataset<?>> extends ASampleAlgorithmFactory<D, StratifiedSampling<D>> implements IRerunnableSamplingAlgorithmFactory<D, StratifiedSampling<D>> {

	@Override
	public StratifiedSampling<D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		LabelBasedStratifiedSampling<D> sampling = new LabelBasedStratifiedSampling<>(random, inputDataset);
		sampling.setSampleSize(sampleSize);
		return sampling;
	}

	@Override
	public void setPreviousRun(final StratifiedSampling<D> previousRun) {
		/* ignore this */
	}

}
