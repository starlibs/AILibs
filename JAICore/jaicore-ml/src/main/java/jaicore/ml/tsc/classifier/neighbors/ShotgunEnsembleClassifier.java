package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.classifier.neighbors.ShotgunEnsembleLearnerAlgorithm.IShotgunEnsembleLearnerConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ITimeSeriesDistance;
import jaicore.ml.tsc.distances.ShotgunDistance;

/**
 * Implementation of Shotgun Ensemble Classifier as published in "Towards Time
 * Series Classfication without Human Preprocessing" by Patrick Schäfer (2014).
 *
 * The Shotgun Classifier is based 1-NN and the Shotgun Distance.
 *
 * The Shotgun Ensemble Algorithm {@link ShotgunEnsembleAlgoritm} determines for
 * specific window lengths the number of correct predicitions on the training
 * data using the leave-one-out technique. The <code>bestScore</code> is the
 * highest number of correct predicitions over all window lengths. Given a
 * <code>factor</code> in <code>(0,1]</code>, the window lengths where
 * <code>correct * factor > bestScore</code> are used in an ensemble of Shotgun
 * Classifiers to create an overall predicition.
 *
 * @author fischor
 */
public class ShotgunEnsembleClassifier extends ASimplifiedTSClassifier<Integer> {

	/**
	 * Factor used to determine whether or not to include a window length into the
	 * overall predicition.
	 */
	protected double factor;

	/** Value matrix containing the time series instances. Set by algorithm. */
	protected double[][] values;

	/** Target values for the instances. Set by the algorithm. */
	protected int[] targets;

	/**
	 * The nearest neighbor classifier used for prediction. Set by the algorithm.
	 */
	protected NearestNeighborClassifier nearestNeighborClassifier;

	/**
	 * The Shotgun Distance used by the {@link #nearestNeighborClassifier}. Set by
	 * the algorithm.
	 */
	protected ShotgunDistance shotgunDistance;

	/**
	 * Holds pairs of (number of correct predictions, window length) obtained in
	 * training phase.
	 */
	protected ArrayList<Pair<Integer, Integer>> windows;

	/**
	 * The best score. States the highest number of correct predicitions for every
	 * window length used in training phase (leave-one-out).
	 */
	protected int bestScore;

	private final IShotgunEnsembleLearnerConfig config;

	/**
	 * Creates a Shotgun Ensemble classifier.
	 *
	 * @param algorithm The training algorithm.
	 * @param factor    Factor used to determine whether or not to include a window
	 *                  length into the overall predicition.
	 */
	public ShotgunEnsembleClassifier(final int minWindowLength, final int maxWindowLength, final boolean meanNormalization, final double factor) {
		super();
		this.config = ConfigCache.getOrCreate(IShotgunEnsembleLearnerConfig.class);
		if (minWindowLength < 1) {
			throw new IllegalArgumentException("The parameter minWindowLength must be greater equal to 1.");
		}
		if (maxWindowLength < 1) {
			throw new IllegalArgumentException("The parameter maxWindowLength must be greater equal to 1.");
		}
		if (minWindowLength > maxWindowLength) {
			throw new IllegalAccessError(
					"The parameter maxWindowsLength must be greater equal to parameter minWindowLength");
		}
		this.config.setProperty(IShotgunEnsembleLearnerConfig.K_WINDOWLENGTH_MIN, "" + minWindowLength);
		this.config.setProperty(IShotgunEnsembleLearnerConfig.K_WINDOWLENGTH_MAX, "" + maxWindowLength);
		this.config.setProperty(IShotgunEnsembleLearnerConfig.K_MEANNORMALIZATION, "" + meanNormalization);
		if ((factor <= 0) || (factor > 1)) {
			throw new IllegalArgumentException("The parameter factor must be in (0,1]");
		}
		this.factor = factor;
	}

	/**
	 * Calculates predicitions for a test instance using 1NN with Shotgun Distance
	 * and different window lengths.
	 *
	 * @param testInstance The test instance.
	 * @return Map of (window length, prediciton) pairs.
	 * @throws PredictionException
	 */
	protected Map<Integer, Integer> calculateWindowLengthPredictions(final double[] testInstance) throws PredictionException {
		// Map holding (windowLength, predicition for instance) pairs.
		Map<Integer, Integer> windowLengthPredicitions = new HashMap<>();

		for (Pair<Integer, Integer> window : this.windows) {
			int correct = window.getX();
			int windowLength = window.getY();
			this.shotgunDistance.setWindowLength(windowLength);
			if (correct > this.bestScore * this.factor) {
				int prediction = this.nearestNeighborClassifier.predict(testInstance);
				windowLengthPredicitions.put(windowLength, prediction);
			}
		}
		return windowLengthPredicitions;
	}

	/**
	 * Returns the most frequent predicition given a Map of (window length,
	 * prediciton) pairs.
	 *
	 * @param windowLengthPredicitions Map of (window length, prediciton) pairs.
	 * @return The most frequent predicition.
	 */
	protected Integer mostFrequentLabelFromWindowLengthPredicitions(final Map<Integer, Integer> windowLengthPredicitions) {
		// Count frequency for labels.
		Map<Integer, Integer> labelFrequencyMap = new HashMap<>();
		for (Integer label : windowLengthPredicitions.values()) {
			if (labelFrequencyMap.containsKey(label)) {
				labelFrequencyMap.put(label, labelFrequencyMap.get(label) + 1);
			} else {
				labelFrequencyMap.put(label, 1);
			}
		}
		// Return most frequent label.
		int topFrequency = -1;
		int mostFrequentLabel = 0;
		for (Integer label : labelFrequencyMap.keySet()) {
			int labelFrequency = labelFrequencyMap.get(label);
			if (labelFrequency > topFrequency) {
				topFrequency = labelFrequency;
				mostFrequentLabel = label;
			}
		}
		return mostFrequentLabel;
	}

	/**
	 * Calculates predicitions for a test dataset using 1NN with Shotgun Distance
	 * and different window lengths.
	 *
	 * @param dataset The dataset to predict for.
	 * @return Map of (window length, predicitions) pairs.
	 * @throws PredictionException
	 */
	protected Map<Integer, List<Integer>> calculateWindowLengthPredictions(final TimeSeriesDataset dataset)
			throws PredictionException {
		// Map holding (windowLength, prediction for dataset) pairs.
		Map<Integer, List<Integer>> windowLengthPredicitions = new HashMap<>();

		for (Pair<Integer, Integer> window : this.windows) {
			int correct = window.getX();
			int windowLength = window.getY();
			this.shotgunDistance.setWindowLength(windowLength);
			if (correct > this.bestScore * this.factor) {
				List<Integer> predictions = this.nearestNeighborClassifier.predict(dataset);
				windowLengthPredicitions.put(windowLength, predictions);
			}
		}
		return windowLengthPredicitions;
	}

	/**
	 * Returns for each instance the most frequent predicitions as contained in a
	 * Map of (window length, list of prediciton for each instance) pairs.
	 *
	 * @param windowLengthPredicitions Map of (window length, list of prediciton for
	 *                                 each instance) pairs.
	 * @return The most frequent predicition for each instace.
	 */
	protected List<Integer> mostFrequentLabelsFromWindowLengthPredicitions(
			final Map<Integer, List<Integer>> windowLengthPredicitions) {
		// Return most frequent label for each instance.
		int numberOfInstances = 0;
		for (List<Integer> labels : windowLengthPredicitions.values()) {
			numberOfInstances = labels.size();
			break;
		}
		List<Integer> predicitions = new ArrayList<>(numberOfInstances);
		for (int i = 0; i < numberOfInstances; i++) {
			// Map holding (windowLength, predicition for instance) pairs.
			Map<Integer, Integer> windowLabelsForInstance = new HashMap<>();
			for (int windowLength : windowLengthPredicitions.keySet()) {
				int predictionForWindowLength = windowLengthPredicitions.get(windowLength).get(i);
				windowLabelsForInstance.put(windowLength, predictionForWindowLength);
			}
			int mostFrequentLabelForInstance = this.mostFrequentLabelFromWindowLengthPredicitions(windowLabelsForInstance);
			predicitions.add(mostFrequentLabelForInstance);
		}

		return predicitions;
	}

	/**
	 * Predicts on univariate instance.
	 *
	 * @param univInstance The univariate instance.
	 * @return Class prediction for the instance.
	 */
	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		if (univInstance == null) {
			throw new IllegalArgumentException("Instance to predict must not be null.");
		}
		Map<Integer, Integer> windowLengthPredicitions = this.calculateWindowLengthPredictions(univInstance);
		int prediciton = this.mostFrequentLabelFromWindowLengthPredicitions(windowLengthPredicitions);
		return prediciton;
	}

	/**
	 * Predicts on a multivariate instance. This is not supported yet.
	 *
	 * @param multivInstance The multivariate instance.
	 * @return Class prediciton for the instance.
	 */
	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		throw new PredictionException("Can't predict on multivariate data yet.");
	}

	/**
	 * Predicts on a dataset.
	 *
	 * @param dataset The dataset.
	 * @return List of class predicitons for each instance of the dataset.
	 */
	@Override
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		// Parameter checks.
		if (dataset == null) {
			throw new IllegalArgumentException("Dataset must not be null.");
		}
		double[][] testInstances = dataset.getValuesOrNull(0);
		if (testInstances == null) {
			throw new PredictionException("Can't predict on empty dataset.");
		}
		// Calculate predictions.
		Map<Integer, List<Integer>> windowLengthPredicitions = this.calculateWindowLengthPredictions(dataset);
		List<Integer> predictions = this.mostFrequentLabelsFromWindowLengthPredicitions(windowLengthPredicitions);
		return predictions;
	}

	/**
	 * Sets the value matrix.
	 *
	 * @param values
	 */
	protected void setValues(final double[][] values) {
		if (values == null) {
			throw new IllegalArgumentException("Values must not be null");
		}
		this.values = values;
	}

	/**
	 * Sets the targets.
	 *
	 * @param targets
	 */
	protected void setTargets(final int[] targets) {
		if (targets == null) {
			throw new IllegalArgumentException("Targets must not be null");
		}
		this.targets = targets;
	}

	/**
	 * Sets the windows and also retreives and sets the @see #bestScore from these
	 * windows.
	 *
	 * @param windows @see #windows
	 */
	protected void setWindows(final ArrayList<Pair<Integer, Integer>> windows) {
		this.windows = windows;
		// Best score.
		int bestScore = -1;
		for (Pair<Integer, Integer> window : windows) {
			int correct = window.getX();
			if (correct > bestScore) {
				bestScore = correct;
			}
		}
		this.bestScore = bestScore;
	}

	/**
	 * Sets the nearest neighbor classifier, {@link #nearestNeighborClassifier}.
	 *
	 * @param nearestNeighborClassifier
	 */
	protected void setNearestNeighborClassifier(final NearestNeighborClassifier nearestNeighborClassifier) {
		ITimeSeriesDistance distanceMeasure = nearestNeighborClassifier.getDistanceMeasure();
		if (!(distanceMeasure instanceof ShotgunDistance)) {
			throw new IllegalArgumentException(
					"The nearest neighbor classifier must use a ShotgunDistance as dsitance measure.");
		} else {
			this.shotgunDistance = (ShotgunDistance) distanceMeasure;
		}
		this.nearestNeighborClassifier = nearestNeighborClassifier;
	}

	@Override
	public ShotgunEnsembleLearnerAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new ShotgunEnsembleLearnerAlgorithm(this.config, this, dataset);
	}

}