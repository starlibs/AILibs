package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory<I extends IInstance>
		implements IRerunnableSamplingAlgorithmFactory<I, LocalCaseControlSampling<I>> {

	private LocalCaseControlSampling<I> previousRun = null;
	private int preSampleSize = -1;

	@Override
	public void setPreviousRun(LocalCaseControlSampling<I> previousRun) {
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
	public LocalCaseControlSampling<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		LocalCaseControlSampling<I> localCaseControlSampling = new LocalCaseControlSampling<>(random,
				this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			localCaseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			localCaseControlSampling.setChosenInstance(previousRun.getChosenInstance());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}

}
