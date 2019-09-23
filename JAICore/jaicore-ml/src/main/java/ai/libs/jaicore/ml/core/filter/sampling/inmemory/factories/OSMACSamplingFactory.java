package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, OSMAC<I, D>> {

	private OSMAC<I, D> previousRun;
	private int preSampleSize = -1;

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
	public void setPreviousRun(final OSMAC<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		OSMAC<I, D> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(this.previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
