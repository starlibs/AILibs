package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graph.TreeNode;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTreeLearningAlgorithm.ITimeSeriesTreeConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature;

/**
 * Time series tree as described in Deng, Houtao et al. "A Time Series Forest
 * for Classification and Feature Extraction." Inf. Sci. 239 (2013): 142-153.
 *
 * This classifier only supports univariate time series prediction.
 *
 * @author Julian Lienen
 *
 */
public class TimeSeriesTreeClassifier extends ASimplifiedTSClassifier<Integer> {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTreeClassifier.class);
	private final ITimeSeriesTreeConfig config;

	/**
	 * Decision information for a tree node within a <code>TimeSeriesTree</code>.
	 */
	static class TimeSeriesTreeNodeDecisionFunction {
		protected TimeSeriesFeature.FeatureType f;
		protected int t1;
		protected int t2;
		protected double threshold;
		protected int classPrediction = -1;

		@Override
		public String toString() {
			return "TimeSeriesTreeNodeDecisionFunction [f=" + this.f + ", t1=" + this.t1 + ", t2=" + this.t2 + ", threshold=" + this.threshold + ", classPrediction=" + this.classPrediction + "]";
		}
	}

	/**
	 * The root node of the time series tree
	 */
	private final TreeNode<TimeSeriesTreeNodeDecisionFunction> rootNode;

	/**
	 * Constructs an empty time series tree.
	 *
	 */
	public TimeSeriesTreeClassifier(final ITimeSeriesTreeConfig config) {
		this.config = config;
		this.rootNode = new TreeNode<>(new TimeSeriesTreeNodeDecisionFunction(), null);
	}

	/**
	 * Getter for the root node.
	 *
	 * @return Returns the root node of the time series tree
	 */
	public TreeNode<TimeSeriesTreeNodeDecisionFunction> getRootNode() {
		return this.rootNode;
	}

	/**
	 * Predicts the class of the given univariate instance by iterating through the
	 * tree starting from the root node to a leaf node to induce a class prediction.
	 *
	 * @param univInstance
	 *            Univariate instance to be predicted
	 */
	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

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

		if (dataset.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate instances are not supported yet.");
		}

		if (dataset.isEmpty()) {
			throw new IllegalArgumentException("The dataset to be predicted must not be null!");
		}

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
	public static TreeNode<TimeSeriesTreeNodeDecisionFunction> decide(final TreeNode<TimeSeriesTreeNodeDecisionFunction> treeNode, final double[] instance) {
		if (treeNode.getValue().classPrediction != -1) {
			return null;
		}

		if (treeNode.getChildren().size() != 2) {
			throw new IllegalStateException("A binary tree node assumed to be complete has not two children nodes.");
		}

		// Check decision function
		if (TimeSeriesFeature.calculateFeature(treeNode.getValue().f, instance, treeNode.getValue().t1, treeNode.getValue().t2, TimeSeriesTreeLearningAlgorithm.USE_BIAS_CORRECTION) <= treeNode.getValue().threshold) {
			return treeNode.getChildren().get(0);
		} else {
			return treeNode.getChildren().get(1);
		}
	}

	@Override
	public TimeSeriesTreeLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new TimeSeriesTreeLearningAlgorithm(this.config, this, dataset);
	}
}
