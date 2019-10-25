package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory implements IRerunnableSamplingAlgorithmFactory<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset, OSMAC<ISingleLabelClassificationDataset>> {

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
		OSMAC<ISingleLabelClassificationDataset> osmac = new OSMAC<>(random, this.preSampleSize, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			osmac.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
			osmac.setChosenInstance(this.previousRun.getChosenInstance());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
