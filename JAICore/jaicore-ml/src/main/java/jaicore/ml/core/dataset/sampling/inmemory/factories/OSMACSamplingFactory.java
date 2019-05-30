package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.inmemory.casecontrol.OSMAC;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory<I extends ILabeledAttributeArrayInstance<?>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, OSMAC<I, D>> {

	private OSMAC<I, D> previousRun;
	private int preSampleSize = -1;

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
	public void setPreviousRun(OSMAC<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		OSMAC<I, D> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
