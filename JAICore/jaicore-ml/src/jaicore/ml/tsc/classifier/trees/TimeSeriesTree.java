package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.List;

import jaicore.graph.TreeNode;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public class TimeSeriesTree extends ASimplifiedTSClassifier<Integer> {
	/**
	 * Decision information for a tree node within a <code>TimeSeriesTree</code>.
	 */
	static class TimeSeriesTreeNodeDecisionFunction {
		int f;
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

	private final TreeNode<TimeSeriesTreeNodeDecisionFunction> rootNode;

	public TimeSeriesTree(final int maxDepth, final int seed) {
		super(new TimeSeriesTreeAlgorithm(maxDepth, seed));
		this.rootNode = new TreeNode<TimeSeriesTreeNodeDecisionFunction>(new TimeSeriesTreeNodeDecisionFunction(),
				null);
	}

	public TreeNode<TimeSeriesTreeNodeDecisionFunction> getRootNode() {
		return rootNode;
	}

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		TreeNode<TimeSeriesTreeNodeDecisionFunction> currNode = this.rootNode;
		TreeNode<TimeSeriesTreeNodeDecisionFunction> tmpNode;
		while ((tmpNode = decide(currNode, univInstance)) != null) {
			currNode = tmpNode;
		}
		return currNode.getValue().classPrediction;
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Multivariate instances are not supported yet.");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO: Multivariate support or exception

		double[][] data = dataset.getValuesOrNull(0);
		List<Integer> predictions = new ArrayList<>();

		for (int i = 0; i < data.length; i++) {
			predictions.add(this.predict(data[i]));
		}

		return predictions;
	}

	public static TreeNode<TimeSeriesTreeNodeDecisionFunction> decide(
			final TreeNode<TimeSeriesTreeNodeDecisionFunction> treeNode, final double[] instance) {
		if (treeNode.getValue().classPrediction != -1)
			return null;

		if (treeNode.getChildren().size() != 2) {
			throw new IllegalStateException("A binary tree node assumed to be complete has not two children nodes.");
		}

		// Check decision function
		if (TimeSeriesTreeAlgorithm.calculateFeature(treeNode.getValue().f, instance, treeNode.getValue().t1,
				treeNode.getValue().t2) <= treeNode.getValue().threshold) {
			return treeNode.getChildren().get(0);
		} else {
			return treeNode.getChildren().get(1);
		}
	}
}
