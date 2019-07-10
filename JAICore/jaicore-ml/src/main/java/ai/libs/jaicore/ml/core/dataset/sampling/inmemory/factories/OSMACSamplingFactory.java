package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.IDataset;
import org.api4.java.ai.ml.ILabeledAttributeArrayInstance;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory<I extends ILabeledAttributeArrayInstance<?>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, OSMAC<I, D>> {

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
