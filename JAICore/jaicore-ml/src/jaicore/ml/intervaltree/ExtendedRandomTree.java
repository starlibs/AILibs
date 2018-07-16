package jaicore.ml.intervaltree;

import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

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
	private double fEmpty;
	private double[][] observations;
	private double[][] intervalSizes;
	private HashMap<Set<Integer>, Double> componentsForSubsets;
	private HashMap<Set<Integer>, Double> varianceOfSubsetIndividual, varianceOfSubsetTotal;

	public ExtendedRandomTree() {
		super();
		this.partitioning = new HashMap<Tree, FeatureSpace>();
		this.sizeOfPartitions = new HashMap<Tree, Double>();
		this.leaves = new ArrayList<Tree>();
		componentsForSubsets = new HashMap<Set<Integer>, Double>();
		varianceOfSubsetTotal = new HashMap<Set<Integer>, Double>();
		varianceOfSubsetIndividual = new HashMap<Set<Integer>, Double>();
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
	}

	public FeatureSpace getFeatureSpace() {
		return this.featureSpace;
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
				productOfFractions *= (partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
						/ featureSpace.getFeatureDomain(j).getRangeSize());
			}
			double prediction;
			if (leaf.getClassDistribution() != null) {
				prediction = leaf.getClassDistribution()[0];
			} else
				prediction = 1.0;
			productOfFractions *= prediction;
			meanAcrossDomain += productOfFractions;
			System.out.println("prediction = " + prediction);
		}
		System.out.println("mean across domain = " + meanAcrossDomain);
		fEmpty = meanAcrossDomain;
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
			double squareOfDifference = Math.pow((prediction - meanAcrossDomain), 2);
			productOfFractions *= squareOfDifference;
			var += productOfFractions;
			System.out.println("total variance = " + var);
		}
		this.totalVariance = var;
		return var;
	}

	/**
	 * Computes variance contribution of a subset of features
	 * 
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalForSubsetOfFeatures(HashSet<Integer> features) {
		double fraction = 0;
		// create cartesian product of observations for the features
		List<Set<Double>> observationSet = new LinkedList<Set<Double>>();
		for (int featureIndex : features) {
			List list = Arrays.stream(observations[featureIndex]).boxed().collect(Collectors.toList());
			HashSet<Double> hSet = new HashSet<Double>();
			hSet.addAll(list);
			observationSet.add(hSet);
		}
		Set<List<Double>> observationProduct = Sets.cartesianProduct(observationSet);
		double vU = 0.0d;
		List<Double> marginals = new LinkedList<Double>();
		System.out.println("size of obsprod = " + observationProduct.size());
		for (List<Double> curObs : observationProduct) {
			ArrayList<Integer> featureList = new ArrayList<Integer>();
			featureList.addAll(features);
			Collections.sort(featureList);
			double marginalPrediction = this.getMarginalPrediction(featureList, curObs);
			marginals.add(marginalPrediction);
			// System.out.println("marg = " + marginalPrediction);
			// weightedSum += (1.0 / featureSpace.getRangeSizeOfFeatureSubspace(features)) *
			// marginalPrediction;
			// weightedSumOfSquares += (1.0 /
			// featureSpace.getRangeSizeOfFeatureSubspace(features))
			// * (marginalPrediction * marginalPrediction);
			double fU = 0.0d;
			// is this correct? Does fEmpty need to be substracted?
			fU = marginalPrediction - fEmpty;
			if (fU < 0.0d)
				fU = 0.0d;
			vU += (1.0 / featureSpace.getRangeSizeOfFeatureSubspace(features)) * (fU * fU);
		}
		System.out.println("factor: " + (1.0 / featureSpace.getRangeSizeOfFeatureSubspace(features)));

		varianceOfSubsetTotal.put(features, vU);
		System.out.println("current total variance = " + vU);
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (varianceOfSubsetTotal.containsKey(subset)) {
					// subtract variances of all feature subsets
					vU -= varianceOfSubsetTotal.get(subset);
					System.out.println("subtracting " + varianceOfSubsetTotal.get(subset));
				}
			}
		}
		fraction = vU / totalVariance;
		componentsForSubsets.put(features, fraction);
		return fraction;
	}

	/**
	 * Computes a marginal prediction for a subset of features and a set of
	 * observations (\hat{a}_U in the paper)
	 * 
	 * @param indices
	 * @param observations
	 * @return Marginal prediction for the subset of features.
	 */
	private double getMarginalPrediction(Set<Integer> indices, double[] observations) {
		double result = 0;
		for (Tree leaf : leaves) {
			if (!observationConsistentWithLeaf(indices, observations, leaf)) {
				continue;
			}
			double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfAllButSubset(indices);
			double sizeOfDomain = featureSpace.getRangeSizeOfAllButSubset(indices);
			double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
			double prediction;
			if (leaf.getClassDistribution() != null) {
				prediction = leaf.getClassDistribution()[0];
			} else
				prediction = 1.0;
			result += prediction * fractionOfSpaceForThisLeaf;
		}
		return result;
	}

	/**
	 * Computes a marginal prediction for a subset of features and a set of
	 * observations (\hat{a}_U in the paper)
	 * 
	 * @param indices
	 * @param observations
	 * @return Marginal prediction for the subset of features.
	 */
	private double getMarginalPrediction(List<Integer> indices, List<Double> observations) {
		double result = 0;
		int numConsistent = 0;
		Set<Integer> subset = new HashSet<Integer>();
		subset.addAll(indices);
		for (Tree leaf : leaves) {
			if (observationConsistentWithLeaf(indices, observations, leaf)) {
				numConsistent++;
				double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfAllButSubset(subset);
				double sizeOfDomain = featureSpace.getRangeSizeOfAllButSubset(subset);
				double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
				double prediction;
				if (leaf.getClassDistribution() != null) {
					prediction = leaf.getClassDistribution()[0];
				} else
					prediction = 1.0;
				result += prediction * fractionOfSpaceForThisLeaf;
			}
		}
		// System.out.println("indices: " + indices + " num consistent: " +
		// numConsistent);
		return result;
	}

	/**
	 * Checks whether all observations are consistent with the feature space
	 * associated with a leaf
	 * 
	 * @param indices
	 * @param observations
	 * @param leaf
	 * @return true of the observations are consistent with the leaf in the
	 *         specified feature dimensions, false otherwise.
	 */
	private boolean observationConsistentWithLeaf(Set<Integer> indices, double[] observations, Tree leaf) {
		if (indices == null && observations == null)
			return true;
		FeatureSpace subSpace = partitioning.get(leaf);
		int[] indicesArr = indices.stream().mapToInt(Number::intValue).toArray();
		for (int i = 0; i < indicesArr.length; i++) {
			int observationIndex = indicesArr[i];
			double value = observations[i];
			if (subSpace.getFeatureDomain(i) instanceof NumericFeatureDomain) {
				NumericFeatureDomain numDom = (NumericFeatureDomain) subSpace.getFeatureDomain(i);
				System.out.println(numDom.getMin() + "," + value + "," + numDom.getMax());

			}
			if (!subSpace.getFeatureDomain(observationIndex).containsInstance(value))
				return false;
		}
		return true;
	}

	/**
	 * checks whether the observations are consistent with a leaf (for implementing
	 * the indicator function in algorithm 2)
	 * 
	 * @param indices
	 * @param observations
	 * @param leaf
	 * @return
	 */
	private boolean observationConsistentWithLeaf(List<Integer> indices, List<Double> observations, Tree leaf) {
		if (indices == null && observations == null)
			return true;
		FeatureSpace subSpace = partitioning.get(leaf);
		// int[] indicesArr = indices.stream().mapToInt(Number::intValue).toArray();
		for (int i = 0; i < indices.size(); i++) {
			int observationIndex = indices.get(i);
			double value = observations.get(i).doubleValue();
			// if(subSpace.getFeatureDomain(i) instanceof NumericFeatureDomain) {
			// NumericFeatureDomain numDom = (NumericFeatureDomain)
			// subSpace.getFeatureDomain(i);
			// System.out.println(numDom.getMin() + ", " + value + ", " + numDom.getMax() +
			// " in there = " + numDom.containsInstance(value));
			//
			// }
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
		if (attribute == -1) {
			double rangeSize = subSpace.getRangeSize();
			leaves.add(node);
			partitioning.put(node, subSpace);
			sizeOfPartitions.put(node, rangeSize);
			// System.out.println("range size: " + rangeSize);
			return;
		}
		// if the split attribute is categorical, remove all but one from the
		// feature space and continue
		else if (subSpace.getFeatureDomain(attribute) instanceof CategoricalFeatureDomain) {
			for (int i = 0; i < children.length; i++) {
				FeatureSpace childSubSpace = new FeatureSpace(subSpace);
				((CategoricalFeatureDomain) childSubSpace.getFeatureDomain(attribute))
						.setValues(new double[] { (double) i });
				computePartitioning(childSubSpace, children[i]);
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
	private void collectSplitPointsAndIntervalSizes(Tree root) {

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
	private void computeObservations() {
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

	private double computeMean(List<Double> data) {
		double result = 0.0d;
		for (double point : data) {
			result += point;
		}
		result /= data.size();
		return result;
	}

	private double computeVariance(List<Double> data) {
		double mean = this.computeMean(data);
		double result = 0.0d;
		for (double point : data) {
			result += (point - mean) * (point - mean);
		}
		result /= (data.size() - 1);
		return result;
	}

	public void preprocess() {
		this.computePartitioning(featureSpace, m_Tree);
		this.computeTotalVariance();
		this.collectSplitPointsAndIntervalSizes(m_Tree);
		this.computeObservations();
		System.out.println("num leaves = " + leaves.size());
	}

}

/**
 * Computes variance contribution of a subset of features
 * 
 * @param features
 * @return Variance contribution of the feature subset
 */
// public double computeMarginalForSubsetOfFeaturesAlternative(HashSet<Integer>
// features) {
// double fraction = 0;
// List<Set<Double>> observationSet = new LinkedList<Set<Double>>();
// for (int featureIndex : features) {
// List list =
// Arrays.stream(observations[featureIndex]).boxed().collect(Collectors.toList());
// HashSet<Double> hSet = new HashSet<Double>();
// hSet.addAll(list);
// observationSet.add(hSet);
// }
//
// List<Set<Double>> sizeSet = new LinkedList<Set<Double>>();
// for (int featureIndex : features) {
// List list =
// Arrays.stream(observations[featureIndex]).boxed().collect(Collectors.toList());
// HashSet<Double> hSet = new HashSet<Double>();
// hSet.addAll(list);
// sizeSet.add(hSet);
// }
//
// System.out.println("Observation set = " + observationSet);
// OrderedSet<List<Double>> observationProduct =
// Sets.cartesianProduct(observationSet);
// Set<List<Double>> sizeProduct = Sets.cartesianProduct(sizeSet);
// double vU = 0.0d;
// double sumOfMargs = 0.0d;
// System.out.println(this.computeTotalVariance());
// // System.out.println("size of obs product = " + observationProduct.size());
// // System.out.println("obs product = " + observationProduct.toString());
// if(observationProduct.size() != sizeProduct.size())
// throw new IllegalArgumentException("Observation and Interval Sizes are not of
// same cardinality!");
// for (int i = 0; i < observationProduct.size(); i++) {
// System.out.println("curObs = " + curObs);
// double marginalPrediction = this.getMarginalPrediction(features, curObs);
// System.out.println("marg = " + marginalPrediction);
// sumOfMargs += marginalPrediction;
// double fU = 0.0d;
// fU = marginalPrediction;
// // System.out.println("fU = " + fU);
// vU += (1.0 / featureSpace.getRangeSizeOfFeatureSubspace(features)) * (fU *
// fU);
// }
// // System.out.println("\nsum of marginals = " + sumOfMargs);
// // System.out.println("V_U = " + vU);
// // System.out.println("V = " + totalVariance);
// assertTrue(vU < totalVariance);
// fraction = vU / totalVariance;
// for (int k = 1; k < features.size(); k++) {
// Set<Set<Integer>> subsets = Sets.combinations(features, k);
// for (Set<Integer> subset : subsets) {
// if (componentsForSubsets.containsKey(subset))
// fraction -= componentsForSubsets.get(subset);
// }
// }
// componentsForSubsets.put(features, fraction);
// return fraction;
// }

// public double computeMarginalForPairsOfFeatures(int featureIndex1, int
// featureIndex2) {
// double fraction = 0;
// Set<Integer> indicatorsOfSubset = new HashSet<Integer>();
//
// indicatorsOfSubset.add(featureIndex1);
// indicatorsOfSubset.add(featureIndex2);
// System.out.println(indicatorsOfSubset);
// double[] curObs = new double[2];
// double vU = 0.0d;
// for (int valueIndex1 = 0; valueIndex1 < observations[featureIndex1].length;
// valueIndex1++) {
// for (int valueIndex2 = 0; valueIndex2 < observations[featureIndex2].length;
// valueIndex2++) {
// curObs[0] = observations[featureIndex1][valueIndex1];
// curObs[1] = observations[featureIndex2][valueIndex2];
// double marginalPrediction = this.getMarginalPrediction(indicatorsOfSubset,
// curObs);
// double fU = 0.0d;
// fU = marginalPrediction - fEmpty;
// vU += 1.0 / featureSpace.getRangeSizeOfFeatureSubspace(indicatorsOfSubset) *
// (fU * fU);
// }
// }
// fraction = vU / totalVariance;
// fraction -= varianceContributions.get(featureIndex1);
// fraction -= varianceContributions.get(featureIndex2);
// return fraction;
// }

// public double computeMarginalForSingleFeature(int featureIndex) {
// double fraction = 0;
// Set<Integer> indicatorsOfSubset = new HashSet<Integer>();
// indicatorsOfSubset.add(featureIndex);
// double[] curObs = new double[1];
// double vU = 0.0d;
// for (int valueIndex = 0; valueIndex < observations[featureIndex].length;
// valueIndex++) {
// curObs[0] = observations[featureIndex][valueIndex];
// double marginalPrediction = this.getMarginalPrediction(indicatorsOfSubset,
// curObs);
// double fU = 0.0d;
// fU = marginalPrediction - fEmpty;
// vU += 1.0 / featureSpace.getRangeSizeOfFeatureSubspace(indicatorsOfSubset) *
// (fU * fU);
// }
// fraction = vU / totalVariance;
// varianceContributions.add(fraction);
// return fraction;
// }