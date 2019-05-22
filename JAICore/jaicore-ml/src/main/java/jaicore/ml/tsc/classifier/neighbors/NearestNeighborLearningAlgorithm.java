package jaicore.ml.tsc.classifier.neighbors;

import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Training algorithm for the nearest neighbors classifier.
 *
 * This algorithm just delegates the value matrix, timestamps and targets to the
 * classifier.
 *
 * @author fischor
 */
public class NearestNeighborLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, NearestNeighborClassifier> {

	protected NearestNeighborLearningAlgorithm(final IAlgorithmConfig config, final NearestNeighborClassifier classifier, final TimeSeriesDataset input) {
		super(config, classifier, input);
	}

	@Override
	public NearestNeighborClassifier call() throws AlgorithmException {
		TimeSeriesDataset dataset = this.getInput();
		if (dataset == null) {
			throw new AlgorithmException("No input data set.");
		}
		if (dataset.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");
		}

		// Retrieve data from dataset.
		double[][] values = dataset.getValuesOrNull(0);
		double[][] timestamps = dataset.getTimestampsOrNull(0);
		int[] targets = dataset.getTargets();
		// Check data.
		if (values == null) {
			throw new AlgorithmException("Empty input data set.");
		}
		if (targets == null) {
			throw new AlgorithmException("Empty targets.");
		}

		// Update model.
		NearestNeighborClassifier model = this.getClassifier();
		model.setValues(values);
		model.setTimestamps(timestamps);
		model.setTargets(targets);
		return model;
	}

	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException();
	}
}