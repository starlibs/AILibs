package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory extends ASampleAlgorithmFactory<ILabeledDataset<?>, LocalCaseControlSampling> implements IRerunnableSamplingAlgorithmFactory<ILabeledDataset<?>, LocalCaseControlSampling> {

	private LocalCaseControlSampling previousRun = null;
	private int preSampleSize = -1;
	private ISingleLabelClassifier pilot;

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

	public ISingleLabelClassifier getPilot() {
		return this.pilot;
	}

	public void setPilot(final ISingleLabelClassifier pilot) {
		this.pilot = pilot;
	}

	@Override
	public LocalCaseControlSampling getAlgorithm(final int sampleSize, final ILabeledDataset<?> inputDataset, final Random random) {
		LocalCaseControlSampling localCaseControlSampling = new LocalCaseControlSampling(random, this.preSampleSize, inputDataset, this.pilot);
		if (this.previousRun != null && this.previousRun.getAcceptanceThresholds() != null) {
			localCaseControlSampling.setAcceptanceTresholds(this.previousRun.getAcceptanceThresholds());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}
}
