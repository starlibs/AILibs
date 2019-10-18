package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.LabelBasedStratifiedSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class LabelBasedStratifiedSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, StratifiedSampling<I, D>> {

	@Override
	public StratifiedSampling<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		LabelBasedStratifiedSampling<I, D> sampling = new LabelBasedStratifiedSampling<>(random, inputDataset);
		sampling.setSampleSize(sampleSize);
		return sampling;
	}

	@Override
	public void setPreviousRun(final StratifiedSampling<I, D> previousRun) {
		/* ignore this */
	}

}
