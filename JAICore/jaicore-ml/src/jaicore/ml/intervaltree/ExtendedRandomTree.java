package jaicore.ml.intervaltree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map.Entry;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.util.RQPHelper;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.classifiers.trees.RandomTree;

/**
 * Extension of a classic RandomTree to predict intervals.
 * 
 * @author mirkoj
 *
 */
public class ExtendedRandomTree extends RandomTree implements RangeQueryPredictor {

	/**
	 * For serialization purposes
	 */
	private static final long serialVersionUID = -467555221387281335L;

	private final IntervalAggregator intervalAggregator;

	public ExtendedRandomTree() {
		this(new AggressiveAggregator());
	}

	public ExtendedRandomTree(IntervalAggregator intervalAggregator) {
		super();
		try {
			this.setOptions(new String[] { "-U" });
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't unprune the tree");
		}
		this.intervalAggregator = intervalAggregator;
	}

	public Interval predictInterval(IntervalAndHeader intervalAndHeader) {
		Interval[] queriedInterval = intervalAndHeader.getIntervals();
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], Tree>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(RQPHelper.getEntry(queriedInterval, m_Tree));

		// the list of all leaf values
		ArrayList<Double> list = new ArrayList<>();
		while (stack.peek() != null) {
			// pick the next node to process
			Entry<Interval[], Tree> toProcess = stack.pop();
			Tree nextTree = toProcess.getValue();
			double threshold = nextTree.getSplitPoint();
			int attribute = nextTree.getAttribute();
			Tree[] children = nextTree.getSuccessors();
			double[] classDistribution = nextTree.getClassDistribution();
			// process node
			if (attribute == -1) {
				// node is a leaf
				// for now, assume that we have regression!
				list.add(classDistribution[0]);
			} else {
				Interval intervalForAttribute = queriedInterval[attribute];
				// no leaf node...
				Tree leftChild = children[0];
				Tree rightChild = children[1];
				// traverse the tree

				if (intervalForAttribute.getLowerBound() <= threshold) {

					if (threshold <= intervalForAttribute.getUpperBound()) {
						// scenario: x_min <= threshold <= x_max
						// query [x_min, threshold] on the left child
						// query [threshold, x_max] right
						Interval[] newInterval = RQPHelper.substituteInterval(toProcess.getKey(),
								new Interval(intervalForAttribute.getLowerBound(), threshold), attribute);
						Interval[] newMaxInterval = RQPHelper.substituteInterval(toProcess.getKey(),
								new Interval(threshold, intervalForAttribute.getUpperBound()), attribute);
						stack.push(RQPHelper.getEntry(newInterval, leftChild));
						stack.push(RQPHelper.getEntry(newMaxInterval, rightChild));
					} else {
						// scenario: threshold <= x_min <= x_max
						// query [x_min, x_max] on the left child
						stack.push(RQPHelper.getEntry(toProcess.getKey(), leftChild));
					}
				}
				// analogously...
				if (intervalForAttribute.getUpperBound() > threshold) {
					stack.push(RQPHelper.getEntry(toProcess.getKey(), rightChild));
				}
			}
		}
		return intervalAggregator.aggregate(list);
	}

}
