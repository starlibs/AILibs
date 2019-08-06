package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory<Y, I extends INumericFeatureInstance & ILabeledInstance<Y>, D extends ISupervisedDataset<Double, Y, I>>
		implements IRerunnableSamplingAlgorithmFactory<Double, Y, I, D, LocalCaseControlSampling<Y, I, D>> {

	private LocalCaseControlSampling<Y, I, D> previousRun = null;
	private int preSampleSize = -1;

	@Override
	public void setPreviousRun(final LocalCaseControlSampling<Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	/**
	 * Set the size of the sample the pilot estimator will be trained with. Default
	 * is half the dataset.
	 *
	 * @param preSampleSize
	 */
	public void setPreSampleSize(final int preSampleSize) {
		this.preSampleSize = preSampleSize;
	}

	@Override
	public LocalCaseControlSampling<Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		LocalCaseControlSampling<Y, I, D> localCaseControlSampling = new LocalCaseControlSampling<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			localCaseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			localCaseControlSampling.setChosenInstance(this.previousRun.getChosenInstance());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}

}
