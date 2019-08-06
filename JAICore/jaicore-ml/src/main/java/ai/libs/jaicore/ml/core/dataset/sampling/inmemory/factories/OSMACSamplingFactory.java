package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory<Y, I extends INumericFeatureInstance & ILabeledInstance<Y>, D extends ISupervisedDataset<Double, Y, I>> implements IRerunnableSamplingAlgorithmFactory<Double, Y, I, D, OSMAC<Y, I, D>> {

	private OSMAC<Y, I, D> previousRun;
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
	public void setPreviousRun(final OSMAC<Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		OSMAC<Y, I, D> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(this.previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
