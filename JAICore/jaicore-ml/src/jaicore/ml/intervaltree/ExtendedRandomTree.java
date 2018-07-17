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
	 * private class for dealing with observations, basically a tuple of doubles.
	 * 
	 * @author jmhansel
	 *
	 */
	private class Observation {
		public double midPoint, intervalSize;

		public Observation(double midPoint, double intervalSize) {
			this.midPoint = midPoint;
			this.intervalSize = intervalSize;
		}
	}

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
	// private double[][] observations;
	// private double[][] intervalSizes;
	private Observation[][] allObservations;
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
			// System.out.println("prediction = " + prediction);
		}
		// System.out.println("mean across domain = " + meanAcrossDomain);
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
			// System.out.println("total variance = " + var);
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
	public double computeMarginalForSubsetOfFeatures(Set<Integer> features) {
		double vU;
		if (varianceOfSubsetTotal.containsKey(features))
			vU = varianceOfSubsetTotal.get(features);
		else
			vU = computeTotalVarianceOfSubset(features);
		System.out.println("current total variance for " + features.toString() + " = " + vU);
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.size() < 1)
					continue;
				if (!varianceOfSubsetIndividual.containsKey(subset)) {
					double temp = computeMarginalForSubsetOfFeatures(subset);
					varianceOfSubsetIndividual.put(subset, temp);
				}
				vU -= varianceOfSubsetIndividual.get(subset);
			}
		}
		varianceOfSubsetIndividual.put(features, vU);
		double fraction = vU / totalVariance;
		// componentsForSubsets.put(features, fraction);
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
	private double getMarginalPrediction(Set<Integer> indices, Observation[] observations) {
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
	private double getMarginalPrediction(List<Integer> indices, List<Observation> observations) {
		double result = 0;
		Set<Integer> subset = new HashSet<Integer>();
		subset.addAll(indices);
		for (Tree leaf : leaves) {
			if (observationConsistentWithLeaf(indices, observations, leaf)) {
				double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfAllButSubset(subset);
				double sizeOfDomain = featureSpace.getRangeSizeOfAllButSubset(subset);
				double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
				// System.out.println("ThetaUi/ThetaU = " + fractionOfSpaceForThisLeaf);
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
	private boolean observationConsistentWithLeaf(Set<Integer> indices, Observation[] observations, Tree leaf) {
		if (indices == null && observations == null)
			return true;
		FeatureSpace subSpace = partitioning.get(leaf);
		int[] indicesArr = indices.stream().mapToInt(Number::intValue).toArray();
		for (int i = 0; i < indicesArr.length; i++) {
			int observationIndex = indicesArr[i];
			double value = observations[i].midPoint;
			if (subSpace.getFeatureDomain(i) instanceof NumericFeatureDomain) {
				NumericFeatureDomain numDom = (NumericFeatureDomain) subSpace.getFeatureDomain(i);
				// System.out.println(numDom.getMin() + "," + value + "," + numDom.getMax());

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
	private boolean observationConsistentWithLeaf(List<Integer> indices, List<Observation> observations, Tree leaf) {
		if (indices == null && observations == null)
			return true;
		FeatureSpace subSpace = partitioning.get(leaf);
		// int[] indicesArr = indices.stream().mapToInt(Number::intValue).toArray();
		for (int i = 0; i < indices.size(); i++) {
			int observationIndex = indices.get(i);
			double value = observations.get(i).midPoint;
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
		allObservations = new Observation[featureSpace.getDimensionality()][];
		// intervalSizes = new double[featureSpace.getDimensionality()][];
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
					allObservations[featureIndex] = new Observation[0];
				} else {
					allObservations[featureIndex] = new Observation[curSplitPoints.size() - 1];
					// intervalSizes[featureIndex] = new double[curSplitPoints.size() - 1];
					for (int lowerIntervalId = 0; lowerIntervalId < curSplitPoints.size() - 1; lowerIntervalId++) {
						allObservations[featureIndex][lowerIntervalId] = new Observation(
								curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1) / 2,
								curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId));
						// intervalSizes[featureIndex][lowerIntervalId] =
						// curSplitPoints.get(lowerIntervalId + 1)
						// - curSplitPoints.get(lowerIntervalId);
					}
				}
			} else if (curDomain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain cDomain = (CategoricalFeatureDomain) curDomain;
				// observations[featureIndex] = cDomain.getValues().clone();
				// intervalSizes[featureIndex] = new double[cDomain.getValues().length];
				// Arrays.fill(intervalSizes[featureIndex], 1.0d);
				allObservations[featureIndex] = new Observation[cDomain.getValues().length];
				for (int i = 0; i < allObservations[featureIndex].length; i++) {
					allObservations[featureIndex][i] = new Observation(cDomain.getValues()[i], 1.0d);
				}
			}
		}
	}

	private double computeTotalVarianceOfSubset(Set<Integer> features) {
		double fraction = 0;
		List<Set<Observation>> observationSet = new LinkedList<Set<Observation>>();
		for (int featureIndex : features) {
			// List<Observation> list =
			// Arrays.stream(allObservations[featureIndex]).boxed().collect(Collectors.toList());
			List<Observation> list = Arrays.stream(allObservations[featureIndex]).collect(Collectors.toList());
			HashSet<Observation> hSet = new HashSet<Observation>();
			hSet.addAll(list);
			observationSet.add(hSet);
		}

		Set<List<Observation>> observationProduct = Sets.cartesianProduct(observationSet);
		// Set<List<Double>> sizesProduct = Sets.cartesianProduct(sizesSet);
		double vU = 0.0d;
		List<Double> marginals = new LinkedList<Double>();
		// System.out.println("size of obsprod = " + observationProduct.size());
		double weightedSum = 0, weightedSumOfSquares = 0;
		double num = 0;
		for (List<Observation> curObs : observationProduct) {
			ArrayList<Integer> featureList = new ArrayList<Integer>();
			featureList.addAll(features);
			Collections.sort(featureList);
			double marginalPrediction = this.getMarginalPrediction(featureList, curObs);
			marginals.add(marginalPrediction);
			// System.out.println(marginalPrediction + ",");
			// double prodOfIntSizes = 1.0d;
			// for (Observation obs : curObs) {
			// prodOfIntSizes *= obs.intervalSize;
			// }
			// double sumOfWeights = this.featureSpace.getRangeSizeOfAllButSubset(features);
			// System.out.println("sum of weights = " + sumOfWeights);
			// System.out.println("prod of interval sizes = " + prodOfIntSizes);
			// double weight = this.featureSpace.getRangeSizeOfFeatureSubspace(features) *
			// prodOfIntSizes;
			// System.out.println("resulting product = " + weight);
			weightedSum += marginalPrediction;
			weightedSumOfSquares += marginalPrediction * marginalPrediction;
			num++;
		}
		weightedSumOfSquares /= num;
		weightedSum /= num;
		vU = weightedSumOfSquares - (weightedSum * weightedSum);
		varianceOfSubsetTotal.put(features, vU);
		return vU;
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
		this.collectSplitPointsAndIntervalSizes(m_Tree);
		this.computeObservations();
		this.computeTotalVariance();
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < this.featureSpace.getDimensionality(); i++) {
			set.add(i);
		}
		this.totalVariance = computeTotalVarianceOfSubset(set);
		System.out.println("trees total variance = " + this.totalVariance);
		System.out.println("num leaves = " + leaves.size());

		double sum = 1.0d;
		for (int i = 0; i < allObservations.length; i++) {
			sum *= allObservations[i].length;
		}
	}
}
