package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.LocalCaseControlSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class LocalCaseControlSamplingFactory extends ASampleAlgorithmFactory<ILabeledDataset<?>, LocalCaseControlSampling> implements IRerunnableSamplingAlgorithmFactory<ILabeledDataset<?>, LocalCaseControlSampling> {

	private LocalCaseControlSampling previousRun = null;
	private int preSampleSize = -1;
	private IClassifier pilot;

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

	public IClassifier getPilot() {
		return this.pilot;
	}

	public void setPilot(final IClassifier pilot) {
		this.pilot = pilot;
	}

	@Override
	public LocalCaseControlSampling getAlgorithm(final int sampleSize, final ILabeledDataset<?> inputDataset, final Random random) {
		if (this.pilot == null) {
			throw new IllegalStateException("No pilot has been defined.");
		}
		if (this.preSampleSize == -1) {
			throw new IllegalStateException("No sample size has been defined for the pilot.");
		}
		LocalCaseControlSampling localCaseControlSampling = new LocalCaseControlSampling(random, this.preSampleSize, inputDataset, this.pilot);
		if (this.previousRun != null && this.previousRun.getAcceptanceThresholds() != null) {
			localCaseControlSampling.setAcceptanceTresholds(this.previousRun.getAcceptanceThresholds());
		}
		localCaseControlSampling.setSampleSize(sampleSize);
		return localCaseControlSampling;
	}
}
