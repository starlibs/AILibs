package jaicore.ml.intervaltree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import jaicore.ml.core.CategoricalFeatureDomain;
import jaicore.ml.core.FeatureDomain;
import jaicore.ml.core.FeatureSpace;
import jaicore.ml.core.Interval;
import jaicore.ml.core.NumericFeatureDomain;
import jaicore.ml.interfaces.Instance;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;

/**
 * Extension of a classic RandomTree to predict intervals.
 * 
 * @author mirkoj
 *
 */
public class ExtendedRandomTree extends RandomTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = -467555221387281335L;
	private FeatureSpace featureSpace;
	private HashMap<Tree, FeatureSpace> partitioning;
	private HashMap<Tree, Double> sizeOfPartitions;
	private ArrayList<Tree> leaves;
	private Instances observations;

	public ExtendedRandomTree() {
		super();
		this.partitioning = new HashMap<Tree, FeatureSpace>();
		this.sizeOfPartitions = new HashMap<Tree, Double>();
		this.leaves = new ArrayList<Tree>();
	}

	public ExtendedRandomTree(FeatureSpace featureSpace) {
		this();
		this.featureSpace = featureSpace;
	}

	public Interval predictInterval(Interval[] queriedInterval) {
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], Tree>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(getEntry(queriedInterval, m_Tree));

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
			Interval intervalForAttribute = queriedInterval[attribute];
			// process node
			if (attribute == -1) {
				// node is a leaf
				// for now, assume that we have regression!
				list.add(classDistribution[0]);
			} else {
				// no leaf node...
				Tree leftChild = children[0];
				Tree rightChild = children[1];
				// traverse the tree
				if (intervalForAttribute.getLowerBound() <= threshold) {
					if (threshold <= intervalForAttribute.getUpperBound()) {
						Interval[] newInterval = substituteInterval(queriedInterval,
								new Interval(intervalForAttribute.getLowerBound(), threshold), attribute);
						stack.push(getEntry(newInterval, leftChild));
					} else {
						stack.push(getEntry(queriedInterval, leftChild));
					}
				}
				if (intervalForAttribute.getUpperBound() > threshold) {
					if (intervalForAttribute.getLowerBound() <= threshold) {
						Interval[] newInterval = substituteInterval(queriedInterval,
								new Interval(threshold, intervalForAttribute.getUpperBound()), attribute);
						stack.push(getEntry(newInterval, rightChild));
					} else {
						stack.push(getEntry(queriedInterval, rightChild));
					}
				}
			}
		}
		return combineInterval(list);
	}

	private Interval combineInterval(ArrayList<Double> list) {
		double min = list.stream().min(Double::compareTo)
				.orElseThrow(() -> new IllegalStateException("Couldn't find minimum?!"));
		double max = list.stream().max(Double::compareTo)
				.orElseThrow(() -> new IllegalStateException("Couldn't find maximum?!"));
		return new Interval(min, max);
	}

	private Interval[] substituteInterval(Interval[] original, Interval toSubstitute, int index) {
		Interval[] copy = Arrays.copyOf(original, original.length);
		copy[index] = toSubstitute;
		return copy;
	}

	private Entry<Interval[], Tree> getEntry(Interval[] interval, Tree tree) {
		return new AbstractMap.SimpleEntry<>(interval, tree);
	}

	public void setFeatureSpace(FeatureSpace featureSpace) {
		this.featureSpace = featureSpace;
		// TODO rework this, maybe remove FeatureSpace class
	}

	public FeatureSpace getFeatureSpace() {
		return this.featureSpace;
	}

	/**
	 * Compute fraction of variance for a single feature.
	 * 
	 * @param featureIndex
	 * @return Fraction of variance explained by this feature.
	 */
	public double getFractionOfVarianceForSingleFeature(int featureIndex) {
		// TODO implement
		return 0.0d;
	}

	/**
	 * Compute fraction of variance for pairs of features.
	 * 
	 * @param featureIndex
	 * @return Fraction of variance explained by this feature.
	 */
	public double getFractionOfVarianceForSingleFeature(int featureIndexA, int featureIndexB) {
		// TODO implement
		return 0.0d;
	}

	/**
	 * Computes the total variance of the partitioned tree
	 * 
	 * @return Total variance
	 */
	private double computeTotalVariance() {
		double var = 0.0d;
		double meanAcrossDomain = 0.0d;
		// compute mean of predicted value across entire domain
		for (Tree leaf : leaves) {
			double productOfFractions = 1.0d;
			for (int j = 0; j < featureSpace.getDimension(); j++) {
				productOfFractions *= partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
						/ featureSpace.getFeatureDomain(j).getRangeSize();
			}
			// in the regression case, this is the predicted value
			productOfFractions *= leaf.getClassDistribution()[0];
			meanAcrossDomain += productOfFractions;
		}
		// compute total variance
		for (Tree leaf : leaves) {
			double productOfFractions = 1.0d;
			for (int j = 0; j < featureSpace.getDimension(); j++) {
				productOfFractions *= partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
						/ featureSpace.getFeatureDomain(j).getRangeSize();
			}
			// in the regression case, this is the predicted value
			productOfFractions *= ((leaf.getClassDistribution()[0] - meanAcrossDomain)
					* (leaf.getClassDistribution()[0] - meanAcrossDomain));
			var += productOfFractions;
		}
		return var;
	}

	/**
	 * Assess importance information for single features using fANOVA
	 * 
	 * @return Array containing importance values for each feature
	 */
	public double[] quantifyImportanceOfSingleFeatures() {
		double importanceValues[] = new double[featureSpace.getDimension()];
		// iterate over all features
		for (int i = 1; i < featureSpace.getDimension(); i++) {
			double singleVariance = 0.0d;
			// iterate over all samples
			for (weka.core.Instance sample : observations) {
				// iterate over leaves to get all partitions
				for (Tree leaf : leaves)
					if (sample.attribute(i).isNumeric()) {
						sample.value(i);
					}
			}
		}
		return importanceValues;
	}

	/**
	 * Computes a partitioning of the feature space for this random tree
	 * 
	 * @param subSpace
	 * @param node
	 */
	private void computePartitioning(FeatureSpace subSpace, Tree node) {

		double splitPoint = node.getSplitPoint();
		int attribute = node.getAttribute();
		Tree[] children = node.getSuccessors();

		// if node is leaf add partition to the map
		if (attribute <= -1) {
			double rangeSize = subSpace.getRangeSize();
			leaves.add(node);
			partitioning.put(node, subSpace);
			sizeOfPartitions.put(node, rangeSize);
			return;
		}
		// if the split element is categorical, remove all but the true one from the
		// feature space and continue
		if (subSpace.getFeatureDomain(attribute) instanceof CategoricalFeatureDomain) {
			for (Tree subtree : children) {
				FeatureSpace childSubSpace = new FeatureSpace(subSpace);
				// TODO remove all but the true elements from the categorical feature domain
				// associated with the split attribute
			}
		}
		// if the split attribute is numeric, set the new interval ranges of the
		// resulting feature space accordingly and continue
		else if (subSpace.getFeatureDomain(attribute) instanceof NumericFeatureDomain) {
			FeatureSpace leftSubSpace = new FeatureSpace(subSpace);
			((NumericFeatureDomain) leftSubSpace.getFeatureDomain(attribute)).setMax(splitPoint);
			FeatureSpace rightSubSpace = new FeatureSpace(subSpace);
			((NumericFeatureDomain) rightSubSpace.getFeatureDomain(attribute)).setMin(splitPoint);
			computePartitioning(leftSubSpace, children[0]);
			computePartitioning(rightSubSpace, children[1]);
		}
	}
}
