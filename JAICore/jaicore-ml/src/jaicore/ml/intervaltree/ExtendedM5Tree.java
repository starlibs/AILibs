package jaicore.ml.intervaltree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map.Entry;

import jaicore.ml.core.Interval;
import weka.classifiers.trees.m5.M5Base;
import weka.classifiers.trees.m5.PreConstructedLinearModel;
import weka.classifiers.trees.m5.RuleNode;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ExtendedM5Tree extends M5Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = -467555221387281335L;

	private Instances queriedDataset = null;
	
	public Interval predictInterval(Instance data) {
		Interval[] mappedData = new Interval[data.numAttributes() / 2];
		int counter = 0;
		ArrayList<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute("bias"));
		for (int attrNum = 0; attrNum < data.numAttributes(); attrNum = attrNum + 2) {
			mappedData[counter] = new Interval(data.value(attrNum), data.value(attrNum + 1));
			attributes.add(new Attribute("xVal"+counter));
			counter++;
		}
		queriedDataset = new Instances("queriedInterval", attributes, 2);
		queriedDataset.setClassIndex(-1);
		return predictInterval(mappedData);
	}

	public Interval predictInterval(Interval[] queriedInterval) {
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], RuleNode>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(getEntry(queriedInterval, this.getM5RootNode()));

		// the list of all leaf values
		ArrayList<Interval> list = new ArrayList<>();
		while (stack.peek() != null) {
			// pick the next node to process
			Entry<Interval[], RuleNode> toProcess = stack.pop();
			RuleNode nextTree = toProcess.getValue();
			double threshold = nextTree.splitVal();
			int attribute = nextTree.splitAtt();
			// process node
			if (nextTree.isLeaf()) {
				Interval [] usedBounds = toProcess.getKey();
				PreConstructedLinearModel model = nextTree.getModel();
				// calculate the values at the edges of the interval
				// we know that by linearity this will yield the extremal values
				Instance instanceLower = new DenseInstance(usedBounds.length+1);
				Instance instanceUpper = new DenseInstance(usedBounds.length+1);
				for (int i = 0; i < usedBounds.length; i++) {
					instanceLower.setValue(i, usedBounds[i].getLowerBound());
					instanceUpper.setValue(i, usedBounds[i].getUpperBound());
				}
				instanceLower.setValue(usedBounds.length, 1);
				instanceUpper.setValue(usedBounds.length, 1);
				instanceLower.setDataset(queriedDataset);
				instanceUpper.setDataset(queriedDataset);
				try {
					double predictionLower = model.classifyInstance(instanceLower);
					double predictionUpper = model.classifyInstance(instanceUpper);

					list.add(new Interval(Double.min(predictionLower, predictionUpper),
							Double.max(predictionLower, predictionUpper)));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				Interval intervalForAttribute = queriedInterval[attribute];
				// no leaf node...
				RuleNode leftChild = nextTree.leftNode();
				RuleNode rightChild = nextTree.rightNode();
				// traverse the tree

				if (intervalForAttribute.getLowerBound() <= threshold) {

					if (threshold <= intervalForAttribute.getUpperBound()) {
						// scenario: x_min <= threshold <= x_max
						// query [x_min, threshold] on the left child
						// query [threshold, x_max] on the right child
						Interval[] leftInterval = substituteInterval(toProcess.getKey(),
								new Interval(intervalForAttribute.getLowerBound(), threshold), attribute);
						stack.push(getEntry(leftInterval, leftChild));
						Interval [] rightInterval = substituteInterval(toProcess.getKey(), new Interval(threshold, intervalForAttribute.getUpperBound()), attribute);
						stack.push(getEntry(rightInterval, rightChild));
					} else {
						// scenario: x_min <= x_max < threshold
						// query [x_min, x_max] on the left child
						stack.push(getEntry(toProcess.getKey(), leftChild));
					}
				} else {
					stack.push(getEntry(toProcess.getKey(), rightChild));
				}
			}
		}
		return combineInterval(list);
	}

	private Interval combineInterval(ArrayList<Interval> list) {
		double min = list.stream().mapToDouble(Interval::getLowerBound).average()
				.orElseThrow(() -> new IllegalStateException("Couldn't find minimum?!"));
		double max = list.stream().mapToDouble(Interval::getUpperBound).average()
				.orElseThrow(() -> new IllegalStateException("Couldn't find maximum?!"));
		return new Interval(min, max);
	}

	private Interval[] substituteInterval(Interval[] original, Interval toSubstitute, int index) {
		Interval[] copy = Arrays.copyOf(original, original.length);
		copy[index] = toSubstitute;
		return copy;
	}

	private Entry<Interval[], RuleNode> getEntry(Interval[] interval, RuleNode tree) {
		return new AbstractMap.SimpleEntry<>(interval, tree);
	}
}
