package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Time series forest classifier as described in Deng, Houtao et al. “A Time
 * Series Forest for Classification and Feature Extraction.” Inf. Sci. 239
 * (2013): 142-153. Consists of mutliple {@link TimeSeriesTree} classifier.
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

	/**
	 * Time time series trees forming the ensemble
	 */
	private TimeSeriesTree[] trees;

	/**
	 * Constructing an untrained ensemble of time series trees.
	 * 
	 * @param numTrees
	 *            Number of trees used in the forest
	 * @param maxDepth
	 *            Maximal depth of the trees to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 */
	public TimeSeriesForestClassifier(final int numTrees, final int maxDepth, final int seed) {
		super(new TimeSeriesForestAlgorithm(numTrees, maxDepth, seed));
		this.trees = new TimeSeriesTree[numTrees];
	}

	/**
	 * Constructing an untrained ensemble of time series trees.
	 * 
	 * @param numTrees
	 *            Number of trees used in the forest
	 * @param maxDepth
	 *            Maximal depth of the trees to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 * @param useFeatureCaching
	 *            Indicator whether feature caching should be used. Since feature
	 *            generation is very efficient, this should be only used if the time
	 *            series is very long
	 * @param numOfCPUs
	 *            Number of CPUs used for the training
	 * @param timeout
	 *            The timeout used for the training
	 */
	public TimeSeriesForestClassifier(final int numTrees, final int maxDepth, final int seed,
			final boolean useFeatureCaching, final int numOfCPUs, final TimeOut timeout) {
		super(new TimeSeriesForestAlgorithm(numTrees, maxDepth, seed, useFeatureCaching));
		this.algorithm.setNumCPUs(numOfCPUs);
		this.algorithm.setTimeout(timeout);
		this.trees = new TimeSeriesTree[numTrees];
	}

	/**
	 * Predicts the class of the given instance by taking the majority vote of all
	 * trees.
	 * 
	 * @param univInstance
	 *            Univariate instance to be predicted
	 */
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		if (!this.isTrained())
			throw new PredictionException("Model has not been built before!");

		if (univInstance == null)
			throw new IllegalArgumentException("Instance to be predicted must not be null or empty!");

		HashMap<Integer, Integer> votes = new HashMap<>();
		for (int i = 0; i < trees.length; i++) {
			int prediction = trees[i].predict(univInstance);
			if (!votes.containsKey(prediction))
				votes.put(prediction, 1);
			else
				votes.replace(prediction, votes.get(prediction) + 1);
		}
		return TimeSeriesUtil.getMaximumKeyByValue(votes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		LOGGER.warn(
				"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return predict(multivInstance.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		if (!this.isTrained())
			throw new PredictionException("Model has not been built before!");

		if (dataset.isMultivariate())
			throw new UnsupportedOperationException("Multivariate instances are not supported yet.");

		if (dataset == null || dataset.isEmpty())
			throw new IllegalArgumentException("Dataset to be predicted must not be null or empty!");

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
	public TimeSeriesTree[] getTrees() {
		return trees;
	}

	/**
	 * Setter for the time series trees.
	 * 
	 * @param trees
	 *            Trees to be set
	 */
	public void setTrees(TimeSeriesTree[] trees) {
		this.trees = trees;
	}

}
