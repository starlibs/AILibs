package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory implements IRerunnableSamplingAlgorithmFactory<ISingleLabelClassificationDataset, OSMAC<ISingleLabelClassificationDataset>> {

	private OSMAC<ISingleLabelClassificationDataset> previousRun;
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
	public void setPreviousRun(final OSMAC<ISingleLabelClassificationDataset> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<ISingleLabelClassificationDataset> getAlgorithm(final int sampleSize, final ISingleLabelClassificationDataset inputDataset, final Random random) {
		Objects.nonNull(inputDataset);
		if (inputDataset.isEmpty()) {
			throw new IllegalArgumentException("Cannot create OSMAC for an empty dataset.");
		}
		OSMAC<ISingleLabelClassificationDataset> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(this.previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
