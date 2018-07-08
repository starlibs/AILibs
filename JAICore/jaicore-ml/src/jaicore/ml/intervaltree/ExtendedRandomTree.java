package jaicore.ml.intervaltree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import jaicore.ml.core.CategoricalFeatureDomain;
import jaicore.ml.core.FeatureDomain;
import jaicore.ml.core.FeatureSpace;
import jaicore.ml.core.Interval;
import jaicore.ml.core.NumericFeatureDomain;
import weka.classifiers.trees.RandomTree;
//import weka.classifiers.trees.RandomTree.Tree;

/**
 * Extension of a classic RandomTree to predict intervals. This class also
 * provides an implementaion of fANOVA based on Hutter et al.s implementation
 * https://github.com/frank-hutter/fanova
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
	// TODO dont use hashmaps
	private HashMap<Tree, FeatureSpace> partitioning;
	private HashMap<Tree, Double> sizeOfPartitions;
	// private HashMap<Tree, Double> midpoints;
	private ArrayList<Tree> leaves;
	private ArrayList<Set<Double>> splitPoints;
	private double totalVariance;
	private double[][] observations;
	private double[][] intervalSizes;

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

		return 0;
	}

	/**
	 * Compute fraction of variance for pairs of features.
	 * 
	 * @param featureIndex
	 * @return Fraction of variance explained by this feature.
	 */
	public double getFractionOfVarianceForPairOfFeatures(int featureIndexA, int featureIndexB) {
		// TODO implement
		return 0;
	}

	/**
	 * Computes the total variance of the partitioned tree
	 * 
	 * @return Total variance
	 */
	public double computeTotalVariance() {
		double var = 0.0d;
		double meanAcrossDomain = 0.0d;
		// compute mean of predicted value across entire domain
		for (Tree leaf : leaves) {
			double productOfFractions = 1.0d;
			for (int j = 0; j < featureSpace.getDimensionality(); j++) {
				productOfFractions *= partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
						/ featureSpace.getFeatureDomain(j).getRangeSize();
			}
			// System.out.println("prod of frac " + productOfFractions);
			// in the regression case, this is the predicted value
			double prediction;
			if (leaf.getClassDistribution() != null)
				prediction = leaf.getClassDistribution()[0];
			else
				prediction = 1.0;
			productOfFractions *= prediction;
			// System.out.println(leaf.toString() + "s distribution: " +
			// leaf.getClassDistribution()[0]);
			meanAcrossDomain += productOfFractions;
			// System.out.println("product of fractions: " + productOfFractions);
			// System.out.println("mean across domain: " + meanAcrossDomain);
		}
		// System.out.println("mean across domain: " + meanAcrossDomain);
		// compute total variance
		for (Tree leaf : leaves) {
			
			double prediction;
			if (leaf.getClassDistribution() != null)
				prediction = leaf.getClassDistribution()[0];
			else
				prediction = 1.0;
			
			double productOfFractions = 1.0d;
			for (int j = 0; j < featureSpace.getDimensionality(); j++) {
				productOfFractions *= partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
						/ featureSpace.getFeatureDomain(j).getRangeSize();
			}
			// in the regression case, this is the predicted value
			productOfFractions *= ((prediction - meanAcrossDomain)
					* (prediction - meanAcrossDomain));
			var += productOfFractions;
		}
		this.totalVariance = var;
		return var;
	}

	/**
	 * Assess importance information for single features using fANOVA
	 * 
	 * @return Array containing importance values for each feature
	 */
	// public double[] quantifyImportanceOfSingleFeatures() {
	// double importanceValues[] = new double[featureSpace.getDimensionality()];
	// // iterate over all features
	// for (int featureIndex = 0; featureIndex < featureSpace.getDimensionality();
	// featureIndex++) {
	// double singleVariance = 0;
	//
	// // iterate over all observations
	// for (int j = 0; j < observations.length; j++) {
	// // iterate over leaves to get all partitions
	// for (Tree leaf : leaves)
	//// if (sample.attribute(i).isNumeric()) {
	//// sample.value(i);
	// }
	// }
	// }
	// return importanceValues;
	// }

	public double computeMarginalForSingleFeature(int featureIndex) {
		double fraction = 0;
		int[] indicesOfObs = { featureIndex };
		double[] curObs = new double[1];
		// TODO new name
		ArrayList<Double> as = new ArrayList<Double>();
		double weightedSum = 0, weightedSumOfSquares = 0;
		for (int valueIndex = 0; valueIndex < observations[featureIndex].length; valueIndex++) {
			curObs[0] = observations[featureIndex][valueIndex];
			double marginalPrediction = this.getMarginalPrediction(indicesOfObs, curObs);
//			System.out.println("marginal prediction: " + marginalPrediction);
			as.add(marginalPrediction);
			double intervalSize = intervalSizes[featureIndex][valueIndex];
			weightedSum += marginalPrediction * intervalSize;
			weightedSumOfSquares += marginalPrediction * marginalPrediction * intervalSize;
		}
		System.out.println("weighted sum: " + weightedSum);
		System.out.println("weighted sum of squares: " + weightedSumOfSquares);
		double marginalVarianceContribution = weightedSumOfSquares - weightedSum * weightedSum;
		// System.out.println("marginal variance contribution: " +
		// marginalVarianceContribution);
		// System.out.println("total variance: " + totalVariance);
		fraction = marginalVarianceContribution / totalVariance;
		return fraction;
	}

	// TODO this function
	private double getMarginalPrediction(int[] indices, double[] observations) {
		double result = 0;
		for (Tree leaf : leaves) {
			if (!observationConsistentWithLeaf(indices, observations, leaf)) {
				continue;
			}
			// double sizeOfLeaf = sizeOfPartitions.get(leaf);
			double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfFeatureSubspace(indices);
			double sizeOfDomain = featureSpace.getRangeSizeOfFeatureSubspace(indices);
			double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
			// System.out.println("size of leaf " + sizeOfLeaf);
			// System.out.println("size of domain " + sizeOfDomain);
			// predicted value c_i
			double prediction;
			if (leaf.getClassDistribution() != null)
				prediction = leaf.getClassDistribution()[0];
			else
				prediction = 1.0;
			// System.out.println("prediction: " + prediction);
			// System.out.println("fraction of space for leave: " +
			// fractionOfSpaceForThisLeaf);
			result += prediction * fractionOfSpaceForThisLeaf;
		}
		return result;
	}

	/**
	 * Checks whether all observations are consistent with the feature space
	 * associated with a leaf
	 * 
	 * @param indices
	 * @param observations
	 * @param leaf
	 * @return
	 */
	public boolean observationConsistentWithLeaf(int indices[], double[] observations, Tree leaf) {
		if (indices == null && observations == null)
			return true;
		FeatureSpace subSpace = partitioning.get(leaf);
		for (int i = 0; i < indices.length; i++) {
			int observationIndex = indices[i];
			double value = observations[i];
			if (!subSpace.getFeatureDomain(observationIndex).containsInstance(value))
				return false;
		}
		return true;
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
			// System.out.println("range size: " + rangeSize);
			return;
		}
		// if the split attribute is categorical, remove all but one from the
		// feature space and continue
		if (subSpace.getFeatureDomain(attribute) instanceof CategoricalFeatureDomain) {
			for (int i = 0; i < children.length; i++) {
				FeatureSpace childSubSpace = new FeatureSpace(subSpace);
				((CategoricalFeatureDomain) childSubSpace.getFeatureDomain(attribute))
						.setValues(new double[] { (double) i });
				computePartitioning(childSubSpace, children[i]);
				// System.out.println("child range size: " +
				// childSubSpace.getFeatureDomain(attribute).getRangeSize());
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

	/**
	 * Collect all split points of a given
	 * 
	 * @param root
	 */
	public void collectSplitPointsAndIntervalSizes(Tree root) {

		// One HashSet of split points for each feature
		splitPoints = new ArrayList<Set<Double>>(featureSpace.getDimensionality());
		for (int i = 0; i < featureSpace.getDimensionality(); i++)
			splitPoints.add(i, new HashSet<Double>());

		Queue<Tree> queueOfNodes = new LinkedList<>();
		queueOfNodes.add(root);

		// While the queue is not empty
		while (!queueOfNodes.isEmpty()) {

			Tree node = queueOfNodes.poll();

			// Is node a leaf?
			if (node.getAttribute() <= -1) {
				continue;
			}
			// TODO deal with categorical features()
			splitPoints.get(node.getAttribute()).add(node.getSplitPoint());

			// Add successors to queue
			for (int i = 0; i < node.getSuccessors().length; i++) {
				queueOfNodes.add(node.getSuccessors()[i]);
			}
		}

	}

	/**
	 * Compute observations, i.e. representatives of each equivalence class of
	 * partitions
	 */
	public void computeObservations() {
		observations = new double[featureSpace.getDimensionality()][];
		intervalSizes = new double[featureSpace.getDimensionality()][];
		for (int featureIndex = 0; featureIndex < featureSpace.getDimensionality(); featureIndex++) {
			List<Double> curSplitPoints = new ArrayList<Double>();
			curSplitPoints.addAll(splitPoints.get(featureIndex));
			// curSplitPoints
			FeatureDomain curDomain = featureSpace.getFeatureDomain(featureIndex);
			if (curDomain instanceof NumericFeatureDomain) {
				NumericFeatureDomain curNumDomain = (NumericFeatureDomain) curDomain;
				curSplitPoints.add(curNumDomain.getMin());
				curSplitPoints.add(curNumDomain.getMax());

				Collections.sort(curSplitPoints);
				// System.out.println(curSplitPoints);
				// if the tree does not split on this value, it is not important
				if (curSplitPoints.size() == 0) {
					observations[featureIndex] = new double[0];
					intervalSizes[featureIndex] = new double[0];
				} else {
					observations[featureIndex] = new double[curSplitPoints.size() - 1];
					intervalSizes[featureIndex] = new double[curSplitPoints.size() - 1];
					for (int lowerIntervalId = 0; lowerIntervalId < curSplitPoints.size() - 1; lowerIntervalId++) {
						observations[featureIndex][lowerIntervalId] = (curSplitPoints.get(lowerIntervalId)
								+ curSplitPoints.get(lowerIntervalId + 1)) / 2;
						intervalSizes[featureIndex][lowerIntervalId] = curSplitPoints.get(lowerIntervalId + 1)
								- curSplitPoints.get(lowerIntervalId);
					}
				}
			} else if (curDomain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain cDomain = (CategoricalFeatureDomain) curDomain;
				observations[featureIndex] = cDomain.getValues().clone();
				intervalSizes[featureIndex] = new double[cDomain.getValues().length];
				Arrays.fill(intervalSizes[featureIndex], 1.0d);
			}
		}
	}

	public void preprocess() {
		this.computePartitioning(featureSpace, m_Tree);
		this.computeTotalVariance();
		this.collectSplitPointsAndIntervalSizes(m_Tree);
		this.computeObservations();
	}

	public void printObservations() {
		for (int i = 0; i < observations.length; i++) {
			for (int j = 0; j < observations[i].length; j++) {
				System.out.print(observations[i][j] + " ");
			}
			System.out.println();
		}
	}

	public void printIntervalSizes() {
		for (int i = 0; i < intervalSizes.length; i++) {
			for (int j = 0; j < intervalSizes[i].length; j++) {
				System.out.print(intervalSizes[i][j] + " ");
			}
			System.out.println();
		}
	}

	public void testStuff() {
		for (int i = 0; i < leaves.size(); i++) {
			double range1 = partitioning.get(leaves.get(i)).getRangeSizeOfFeatureSubspace(new int[] { 1 });
			double range2 = featureSpace.getRangeSizeOfFeatureSubspace(new int[] { 1 });
			System.out.println(featureSpace.getFeatureDomain(1));
			System.out.println("leaf range: " + range1);
			System.out.println("all range: " + range2);
		}
	}

}
