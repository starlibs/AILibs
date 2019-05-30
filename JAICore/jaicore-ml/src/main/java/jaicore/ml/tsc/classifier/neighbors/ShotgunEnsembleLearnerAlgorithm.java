package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;

import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ShotgunDistance;

/**
 * Implementation of Shotgun Ensemble Algorihm as published in "Towards Time
 * Series Classfication without Human Preprocessing" by Patrick Schäfer (2014).
 *
 * Given a maximal window length <code>maxWindowLength</code> and a minumum
 * window length <code>minWindowLength</code>, the Shotgun Ensemble algorithm
 * determines for each of the window lengths form <code>maxWindowLength</code>
 * downto <code>minWindowLength</code> the number of correct predicitions on the
 * training data using the leave-one-out technique.
 *
 * @author fischor
 */
public class ShotgunEnsembleLearnerAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, ShotgunEnsembleClassifier> {

	public interface IShotgunEnsembleLearnerConfig extends IAlgorithmConfig {

		public static final String K_WINDOWLENGTH_MIN = "windowlength.min";
		public static final String K_WINDOWLENGTH_MAX = "windowlength.max";
		public static final String K_MEANNORMALIZATION = "meannormalization";

		@Key(K_WINDOWLENGTH_MIN)
		public int windowSizeMin();

		@Key(K_WINDOWLENGTH_MAX)
		public int windowSizeMax();

		@Key(K_MEANNORMALIZATION)
		@DefaultValue("false")
		public boolean meanNormalization();
	}

	public ShotgunEnsembleLearnerAlgorithm(final IShotgunEnsembleLearnerConfig config, final ShotgunEnsembleClassifier classifier, final TimeSeriesDataset dataset) {
		super(config, classifier, dataset);
	}

	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IShotgunEnsembleLearnerConfig getConfig() {
		return (IShotgunEnsembleLearnerConfig) super.getConfig();
	}

	@Override
	public ShotgunEnsembleClassifier call() throws AlgorithmException {
		TimeSeriesDataset dataset = this.getInput();
		if (dataset == null) {
			throw new AlgorithmException("No input data set.");
		}
		if (dataset.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");
		}

		// Retrieve data from dataset.
		double[][] values = dataset.getValuesOrNull(0);
		// Check data.
		if (values == null) {
			throw new AlgorithmException("Empty input data set.");
		}
		int[] targets = dataset.getTargets();
		if (targets == null) {
			throw new AlgorithmException("Empty targets.");
		}

		// Holds pairs of (number of correct predictions, window length).
		ArrayList<Pair<Integer, Integer>> scores = new ArrayList<>();

		for (int windowLength = this.getConfig().windowSizeMax(); windowLength >= this.getConfig().windowSizeMin(); windowLength--) {
			int correct = 0;

			// 1-NN with Leave-One-Out CV.
			ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, this.getConfig().meanNormalization());
			for (int i = 0; i < values.length; i++) {
				// Predict for i-th instance.
				double minDistance = Double.MAX_VALUE;
				int instanceThatMinimizesDistance = -1;
				for (int j = 0; j < values.length; j++) {
					if (i != j) {
						double distance = shotgunDistance.distance(values[i], values[j]);
						if (distance < minDistance) {
							minDistance = distance;
							instanceThatMinimizesDistance = j;
						}
					}
				}
				// Check, if Leave-One-Out prediction for i-th was correct.
				if (targets[i] == targets[instanceThatMinimizesDistance]) {
					correct++;
				}
			}

			scores.add(new Pair<>(correct, windowLength));
		}

		// Update model.
		NearestNeighborClassifier nearestNeighborClassifier = new NearestNeighborClassifier(new ShotgunDistance(this.getConfig().windowSizeMax(), this.getConfig().meanNormalization()));
		try {
			nearestNeighborClassifier.train(dataset);
		} catch (Exception e) {
			throw new AlgorithmException(e, "Cant train nearest neighbor classifier.");
		}

		ShotgunEnsembleClassifier model = this.getClassifier();
		model.setWindows(scores);
		model.setNearestNeighborClassifier(nearestNeighborClassifier);
		return model;
	}
}