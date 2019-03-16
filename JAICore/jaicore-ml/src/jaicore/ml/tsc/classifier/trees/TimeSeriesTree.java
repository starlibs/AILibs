package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graph.TreeNode;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature;

/**
 * Time series tree as described in Deng, Houtao et al. “A Time Series Forest
 * for Classification and Feature Extraction.” Inf. Sci. 239 (2013): 142-153.
 * 
 * This classifier only supports univariate time series prediction.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesTree extends ASimplifiedTSClassifier<Integer> {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTree.class);

	/**
	 * Decision information for a tree node within a <code>TimeSeriesTree</code>.
	 */
	static class TimeSeriesTreeNodeDecisionFunction {
		TimeSeriesFeature.FeatureType f;
		int t1;
		int t2;
		double threshold;
		int classPrediction = -1;

		@Override
		public String toString() {
			return "TimeSeriesTreeNodeDecisionFunction [f=" + f + ", t1=" + t1 + ", t2=" + t2 + ", threshold="
					+ threshold + ", classPrediction=" + classPrediction + "]";
		}
	}

	/**
	 * The root node of the time series tree
	 */
	private final TreeNode<TimeSeriesTreeNodeDecisionFunction> rootNode;

	/**
	 * Constructs an empty time series tree.
	 * 
	 * @param maxDepth
	 *            Maximal depth of the tree to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 */
	public TimeSeriesTree(final int maxDepth, final int seed) {
		super(new TimeSeriesTreeAlgorithm(maxDepth, seed));
		this.rootNode = new TreeNode<TimeSeriesTreeNodeDecisionFunction>(new TimeSeriesTreeNodeDecisionFunction(),
				null);
	}

	/**
	 * Constructs an empty time series tree.
	 * 
	 * @param maxDepth
	 *            Maximal depth of the tree to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 * @param useFeatureCaching
	 *            Indicator whether feature caching should be used. Since feature
	 *            generation is very efficient, this should be only used if the time
	 *            series is very long
	 */
	public TimeSeriesTree(final int maxDepth, final int seed, final boolean useFeatureCaching) {
		super(new TimeSeriesTreeAlgorithm(maxDepth, seed, useFeatureCaching));
		this.rootNode = new TreeNode<TimeSeriesTreeNodeDecisionFunction>(new TimeSeriesTreeNodeDecisionFunction(),
				null);
	}

	/**
	 * Getter for the root node.
	 * 
	 * @return Returns the root node of the time series tree
	 */
	public TreeNode<TimeSeriesTreeNodeDecisionFunction> getRootNode() {
		return rootNode;
	}

	/**
	 * Predicts the class of the given univariate instance by iterating through the
	 * tree starting from the root node to a leaf node to induce a class prediction.
	 * 
	 * @param univInstance
	 *            Univariate instance to be predicted
	 */
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		if (!this.isTrained())
			throw new PredictionException("Model has not been built before!");

		TreeNode<TimeSeriesTreeNodeDecisionFunction> currNode = this.rootNode;
		TreeNode<TimeSeriesTreeNodeDecisionFunction> tmpNode;
		while ((tmpNode = decide(currNode, univInstance)) != null) {
			currNode = tmpNode;
		}
		return currNode.getValue().classPrediction;
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

		if (dataset.isEmpty())
			throw new IllegalArgumentException("The dataset to be predicted must not be null!");

		double[][] data = dataset.getValuesOrNull(0);
		List<Integer> predictions = new ArrayList<>();

		for (int i = 0; i < data.length; i++) {
			predictions.add(this.predict(data[i]));
		}

		return predictions;
	}

	/**
	 * Function performing the decision on a <code>treeNode</code> given the
	 * <code>instance</code> based on the locally stored splitting criterion.
	 * 
	 * @param treeNode
	 *            Tree node where the decision is taken place
	 * @param instance
	 *            Instance values
	 * @return Returns the child node where the next decision can be done, null if
	 *         <code>treeNode</code> is a tree node
	 */
	public static TreeNode<TimeSeriesTreeNodeDecisionFunction> decide(
			final TreeNode<TimeSeriesTreeNodeDecisionFunction> treeNode, final double[] instance) {
		if (treeNode.getValue().classPrediction != -1)
			return null;

		if (treeNode.getChildren().size() != 2) {
			throw new IllegalStateException("A binary tree node assumed to be complete has not two children nodes.");
		}

		// Check decision function
		if (TimeSeriesFeature.calculateFeature(treeNode.getValue().f, instance, treeNode.getValue().t1,
				treeNode.getValue().t2, TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION) <= treeNode.getValue().threshold) {
			return treeNode.getChildren().get(0);
		} else {
			return treeNode.getChildren().get(1);
		}
	}
}
