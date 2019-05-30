package jaicore.ml.tsc.classifier;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public abstract class ASimplifiedTSCLearningAlgorithm<T, C extends ASimplifiedTSClassifier<T>> extends AAlgorithm<TimeSeriesDataset, C> {
	protected ASimplifiedTSCLearningAlgorithm(final IAlgorithmConfig config, final C classifier, final TimeSeriesDataset input) {
		super(config, input);
		this.classifier = classifier; // this is the classifier that is being trained (and outputted in the end)
	}

	/**
	 * The model which is maintained during algorithm calls
	 */
	private final C classifier;

	public C getClassifier() {
		return this.classifier;
	}
}
