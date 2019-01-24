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

	static class TimeSeriesTreeNode extends TreeNode<TimeSeriesTreeNodeDecisionFunction> {
		// TODO: Two children properties exist! Remove one or do not extend TreeNode
		private final List<TimeSeriesTreeNode> children = new ArrayList<>();

		public TimeSeriesTreeNode(TimeSeriesTreeNodeDecisionFunction value,
				TreeNode<TimeSeriesTreeNodeDecisionFunction> parent) {
			super(value, parent);
		}

		public TimeSeriesTreeNode decide(final double[] instance) {

			if (this.getValue().classPrediction != -1)
				return null;

			if (this.children.size() != 2) {
				System.out.println(this.getChildren());
				System.out.println(this.getValue());
				throw new IllegalStateException(
						"A binary tree node assumed to be complete has not two children nodes.");
			}

			// Check decision function
			if (TimeSeriesTreeAlgorithm.calculateFeature(this.getValue().f, instance, this.getValue().t1,
					this.getValue().t2) <= this.getValue().threshold) {
				return this.children.get(0);
			} else {
				return this.children.get(1);
			}
		}

		@Override
		public TimeSeriesTreeNode addChild(TimeSeriesTreeNodeDecisionFunction child) {
			TimeSeriesTreeNode childNode = new TimeSeriesTreeNode(child, this);
			this.children.add(childNode);
			return childNode;
		}
	}

	private final TimeSeriesTreeNode rootNode;

	public TimeSeriesTree(final int maxDepth) {
		super(new TimeSeriesTreeAlgorithm(maxDepth));
		this.rootNode = new TimeSeriesTreeNode(new TimeSeriesTreeNodeDecisionFunction(), null);
	}

	public TimeSeriesTreeNode getRootNode() {
		return rootNode;
	}

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		TimeSeriesTreeNode currNode = this.rootNode;
		TimeSeriesTreeNode tmpNode;
		while ((tmpNode = currNode.decide(univInstance)) != null) {
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

}
