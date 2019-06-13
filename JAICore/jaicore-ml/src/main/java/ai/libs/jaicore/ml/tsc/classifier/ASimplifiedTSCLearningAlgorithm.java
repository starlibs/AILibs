package ai.libs.jaicore.ml.tsc.classifier;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.IAlgorithmConfig;
import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;

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
