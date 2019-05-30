package jaicore.ml.intervaltree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.util.RQPHelper;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.classifiers.trees.m5.M5Base;
import weka.classifiers.trees.m5.PreConstructedLinearModel;
import weka.classifiers.trees.m5.RuleNode;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ExtendedM5Tree extends M5Base implements RangeQueryPredictor {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 6099808075887732225L;

	private final IntervalAggregator intervalAggregator;

	public ExtendedM5Tree() {
		this(new AggressiveAggregator());
	}

	public ExtendedM5Tree(final IntervalAggregator intervalAggregator) {
		super();
		try {
			this.setOptions(new String[] { "-U" });
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't unprune the tree");
		}
		this.intervalAggregator = intervalAggregator;
	}

	@Override
	public Interval predictInterval(final IntervalAndHeader intervalAndHeader) {
		Interval[] queriedInterval = intervalAndHeader.getIntervals();
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], RuleNode>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(RQPHelper.getEntry(queriedInterval, this.getM5RootNode()));

		// the list of all leaf values
		ArrayList<Double> list = new ArrayList<>();
		while (stack.peek() != null) {
			// pick the next node to process
			Entry<Interval[], RuleNode> toProcess = stack.pop();
			RuleNode nextTree = toProcess.getValue();
			double threshold = nextTree.splitVal();
			int attribute = nextTree.splitAtt();
			// process node
			if (nextTree.isLeaf()) {
				this.predictLeaf(list, toProcess, nextTree, intervalAndHeader.getHeaderInformation());
			} else {
				Interval intervalForAttribute = queriedInterval[attribute];
				// no leaf node...
				RuleNode leftChild = nextTree.leftNode();
				RuleNode rightChild = nextTree.rightNode();
				// traverse the tree

				if (intervalForAttribute.getInf() <= threshold) {

					if (threshold <= intervalForAttribute.getSup()) {
						// scenario: x_min <= threshold <= x_max
						// query [x_min, threshold] on the left child
						// query [threshold, x_max] on the right child
						Interval[] leftInterval = RQPHelper.substituteInterval(toProcess.getKey(), new Interval(intervalForAttribute.getInf(), threshold), attribute);
						stack.push(RQPHelper.getEntry(leftInterval, leftChild));
						Interval[] rightInterval = RQPHelper.substituteInterval(toProcess.getKey(), new Interval(threshold, intervalForAttribute.getSup()), attribute);
						stack.push(RQPHelper.getEntry(rightInterval, rightChild));
					} else {
						// scenario: x_min <= x_max < threshold
						// query [x_min, x_max] on the left child
						stack.push(RQPHelper.getEntry(toProcess.getKey(), leftChild));
					}
				} else {
					stack.push(RQPHelper.getEntry(toProcess.getKey(), rightChild));
				}
			}
		}
		return this.intervalAggregator.aggregate(list);
	}

	private void predictLeaf(final ArrayList<Double> list, final Entry<Interval[], RuleNode> toProcess, final RuleNode nextTree, final Instances header) {
		Interval[] usedBounds = toProcess.getKey();
		PreConstructedLinearModel model = nextTree.getModel();
		// calculate the values at the edges of the interval
		// we know that by linearity this will yield the extremal values
		Instance instanceLower = new DenseInstance(usedBounds.length + 1);
		Instance instanceUpper = new DenseInstance(usedBounds.length + 1);

		double[] coefficients = model.coefficients();

		for (int i = 0; i < usedBounds.length; i++) {
			double coefficient = coefficients[i];
			if (coefficient < 0) {
				instanceLower.setValue(i + 1, usedBounds[i].getInf());
				instanceUpper.setValue(i + 1, usedBounds[i].getSup());
			} else {
				instanceLower.setValue(i + 1, usedBounds[i].getSup());
				instanceUpper.setValue(i + 1, usedBounds[i].getInf());
			}
		}
		instanceLower.setValue(0, 1);
		instanceUpper.setValue(0, 1);
		instanceLower.setDataset(header);
		instanceUpper.setDataset(header);
		try {
			double predictionLower = model.classifyInstance(instanceLower);
			double predictionUpper = model.classifyInstance(instanceUpper);
			list.add(predictionLower);
			list.add(predictionUpper);
		} catch (Exception e) {
			throw new PredictionFailedException(e);
		}
	}
}
