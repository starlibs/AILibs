package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory<I extends ILabeledAttributeArrayInstance<?>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, LocalCaseControlSampling<I, D>> {

	private LocalCaseControlSampling<I, D> previousRun = null;
	private int preSampleSize = -1;

	@Override
	public void setPreviousRun(LocalCaseControlSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	/**
	 * Set the size of the sample the pilot estimator will be trained with. Default
	 * is half the dataset.
	 * 
	 * @param preSampleSize
	 */
	public void setPreSampleSize(int preSampleSize) {
		this.preSampleSize = preSampleSize;
	}

	@Override
	public LocalCaseControlSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		LocalCaseControlSampling<I, D> localCaseControlSampling = new LocalCaseControlSampling<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			localCaseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			localCaseControlSampling.setChosenInstance(previousRun.getChosenInstance());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}

}
