package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, LocalCaseControlSampling> {

	private LocalCaseControlSampling previousRun = null;
	private int preSampleSize = -1;

	@Override
	public void setPreviousRun(final LocalCaseControlSampling previousRun) {
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
	public LocalCaseControlSampling getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		LocalCaseControlSampling localCaseControlSampling = new LocalCaseControlSampling(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			localCaseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			localCaseControlSampling.setChosenInstance(this.previousRun.getChosenInstance());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}

}
