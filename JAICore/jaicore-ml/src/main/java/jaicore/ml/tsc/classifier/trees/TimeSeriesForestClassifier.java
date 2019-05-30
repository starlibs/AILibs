package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.classifier.trees.TimeSeriesForestLearningAlgorithm.ITimeSeriesForestConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Time series forest classifier as described in Deng, Houtao et al. "A Time
 * Series Forest for Classification and Feature Extraction." Inf. Sci. 239
 * (2013): 142-153. Consists of mutliple {@link TimeSeriesTreeClassifier} classifier.
 *
 * This classifier only supports univariate time series prediction.
 *
 * @author Julian Lienen
 *
 */
public class TimeSeriesForestClassifier extends ASimplifiedTSClassifier<Integer> {

	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesForestClassifier.class);

	private final ITimeSeriesForestConfig config;

	/**
	 * Time time series trees forming the ensemble
	 */
	private TimeSeriesTreeClassifier[] trees;

	/**
	 * Constructing an untrained ensemble of time series trees.
	 */
	public TimeSeriesForestClassifier() {
		this(ConfigCache.getOrCreate(ITimeSeriesForestConfig.class));
	}

	/**
	 * Constructing an untrained ensemble of time series trees.
	 */
	public TimeSeriesForestClassifier(final ITimeSeriesForestConfig config) {
		this.config = config;
	}

	public void setNumberOfTrees(final int numTrees) {
		this.config.setProperty(ITimeSeriesForestConfig.K_NUMTREES, "" + numTrees);
	}

	public void setMaxDepth(final int maxDepth) {
		this.config.setProperty(ITimeSeriesForestConfig.K_MAXDEPTH, "" + maxDepth);
	}

	public void setFeatureCaching(final boolean enableFeatureCaching) {
		this.config.setProperty(ITimeSeriesForestConfig.K_FEATURECACHING, "" + enableFeatureCaching);
	}

	public void setSeed(final int seed) {
		this.config.setProperty(ITimeSeriesForestConfig.K_SEED, "" + seed);
	}


	/**
	 * Predicts the class of the given instance by taking the majority vote of all
	 * trees.
	 *
	 * @param univInstance
	 *            Univariate instance to be predicted
	 */
	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		if (univInstance == null) {
			throw new IllegalArgumentException("Instance to be predicted must not be null or empty!");
		}

		HashMap<Integer, Integer> votes = new HashMap<>();
		for (int i = 0; i < this.trees.length; i++) {
			int prediction = this.trees[i].predict(univInstance);
			if (!votes.containsKey(prediction)) {
				votes.put(prediction, 1);
			} else {
				votes.replace(prediction, votes.get(prediction) + 1);
			}
		}
		return TimeSeriesUtil.getMaximumKeyByValue(votes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		LOGGER.warn("Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return this.predict(multivInstance.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		if (dataset == null || dataset.isEmpty()) {
			throw new IllegalArgumentException("Dataset to be predicted must not be null or empty!");
		}

		if (dataset.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate instances are not supported yet.");
		}

		double[][] data = dataset.getValuesOrNull(0);
		List<Integer> predictions = new ArrayList<>();
		LOGGER.debug("Starting prediction...");
		for (int i = 0; i < data.length; i++) {
			predictions.add(this.predict(data[i]));
		}
		LOGGER.debug("Finished prediction.");
		return predictions;
	}

	/**
	 * Getter for the time series trees.
	 *
	 * @return Returns an array consisting of all forest trees.
	 */
	public TimeSeriesTreeClassifier[] getTrees() {
		return this.trees;
	}

	/**
	 * Setter for the time series trees.
	 *
	 * @param trees
	 *            Trees to be set
	 */
	public void setTrees(final TimeSeriesTreeClassifier[] trees) {
		this.trees = trees;
	}

	@Override
	public TimeSeriesForestLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new TimeSeriesForestLearningAlgorithm(this.config, this, dataset);
	}
}
