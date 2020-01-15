package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.OSMAC;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class OSMACSamplingFactory extends ASampleAlgorithmFactory<ILabeledDataset<?>, OSMAC<ILabeledDataset<?>>> implements IRerunnableSamplingAlgorithmFactory<ILabeledDataset<?>, OSMAC<ILabeledDataset<?>>> {

	private OSMAC<ILabeledDataset<?>> previousRun;
	private int preSampleSize = -1;
	private IClassifier pilot;

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
	public void setPreviousRun(final OSMAC<ILabeledDataset<?>> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public OSMAC<ILabeledDataset<?>> getAlgorithm(final int sampleSize, final ILabeledDataset<?> inputDataset, final Random random) {
		Objects.requireNonNull(inputDataset);
		Objects.requireNonNull(this.pilot);
		if (inputDataset.isEmpty()) {
			throw new IllegalArgumentException("Cannot create OSMAC for an empty dataset.");
		}
		SimpleRandomSamplingFactory<ILabeledDataset<?>> subSampleFactory = new SimpleRandomSamplingFactory<>();
		subSampleFactory.setRandom(random);
		OSMAC<ILabeledDataset<?>> osmac = new OSMAC<>(random, inputDataset, subSampleFactory, this.preSampleSize, this.pilot);
		if (this.previousRun != null && this.previousRun.getAcceptanceThresholds() != null) {
			osmac.setAcceptanceTresholds(this.previousRun.getAcceptanceThresholds());
		}
		osmac.setSampleSize(sampleSize);
		return osmac;
	}

}
