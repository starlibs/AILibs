package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.util.TimeSeriesUtil;

public class TimeSeriesForestClassifier extends ASimplifiedTSClassifier<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesForestClassifier.class);

	private TimeSeriesTree[] trees;

	public TimeSeriesForestClassifier(final int numTrees, final int maxDepth, final int seed) {
		super(new TimeSeriesForestAlgorithm(numTrees, maxDepth, seed));
		this.trees = new TimeSeriesTree[numTrees];
	}

	public TimeSeriesForestClassifier(final int numTrees, final int maxDepth, final int seed,
			final boolean useFeatureCaching) {
		super(new TimeSeriesForestAlgorithm(numTrees, maxDepth, seed, useFeatureCaching));
		this.trees = new TimeSeriesTree[numTrees];
	}

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
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

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		throw new UnsupportedOperationException("Multivariate instances are not supported yet.");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
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

	public TimeSeriesTree[] getTrees() {
		return trees;
	}

	public void setTrees(TimeSeriesTree[] trees) {
		this.trees = trees;
	}


}
