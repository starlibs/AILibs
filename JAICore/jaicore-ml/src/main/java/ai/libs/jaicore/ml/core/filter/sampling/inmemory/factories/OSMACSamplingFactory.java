package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory<D extends ILabeledDataset<ISingleLabelClassificationInstance>> implements IRerunnableSamplingAlgorithmFactory<ISingleLabelClassificationInstance, D, OSMAC<D>> {

	private OSMAC<D> previousRun;
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
	public void setPreviousRun(final OSMAC<D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		OSMAC<D> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(this.previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
