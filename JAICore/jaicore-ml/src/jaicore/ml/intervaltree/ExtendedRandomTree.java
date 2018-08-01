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

import com.google.common.collect.Lists;
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
		// important, otherwise some classdistributions may be null
		this.setAllowUnclassifiedInstances(false);
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
	// public double computeTotalVariance() {
	// double var = 0.0d;
	// double meanAcrossDomain = 0.0d;
	// // compute mean of predicted value across entire domain
	// for (Tree leaf : leaves) {
	// double productOfFractions = 1.0d;
	// for (int j = 0; j < featureSpace.getDimensionality(); j++) {
	// productOfFractions *=
	// (partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
	// / featureSpace.getFeatureDomain(j).getRangeSize());
	// }
	// double prediction;
	// if (leaf.getClassDistribution() != null) {
	// prediction = leaf.getClassDistribution()[0];
	// } else
	// prediction = 1.0;
	// productOfFractions *= prediction;
	// meanAcrossDomain += productOfFractions;
	// // System.out.println("prediction = " + prediction);
	// }
	// // System.out.println("mean across domain = " + meanAcrossDomain);
	// fEmpty = meanAcrossDomain;
	// // compute total variance
	// for (Tree leaf : leaves) {
	//
	// double prediction;
	// if (leaf.getClassDistribution() != null)
	// prediction = leaf.getClassDistribution()[0];
	// else
	// prediction = 1.0;
	//
	// double productOfFractions = 1.0d;
	// for (int j = 0; j < featureSpace.getDimensionality(); j++) {
	// productOfFractions *=
	// partitioning.get(leaf).getFeatureDomain(j).getRangeSize()
	// / featureSpace.getFeatureDomain(j).getRangeSize();
	// }
	// double squareOfDifference = Math.pow((prediction - meanAcrossDomain), 2);
	// productOfFractions *= squareOfDifference;
	// var += productOfFractions;
	// // System.out.println("total variance = " + var);
	// }
	// this.totalVariance = var;
	// return var;
	// }

	/**
	 * Computes variance contribution of a subset of features
	 * 
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalVarianceContributionForSubsetOfFeatures(Set<Integer> features) {
		double vU;
		if (this.totalVariance == 0.0d) {
			System.out.println("The trees total variance is zero, predictions make no sense at this point!");
			return Double.NaN;
		}
		if (varianceOfSubsetTotal.containsKey(features))
			vU = varianceOfSubsetTotal.get(features);
		else
			vU = computeTotalVarianceOfSubset(features);
		System.out.println("current total variance for " + features.toString() + " = " + vU);
		double subtractor = 0;
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.size() < 1)
					continue;
				if (!varianceOfSubsetIndividual.containsKey(subset)) {
					double temp = computeMarginalVarianceContributionForSubsetOfFeatures(subset);
				}
				// System.out.println("Subtracting " + varianceOfSubsetIndividual.get(subset) +
				// " for " + subset);
				vU -= varianceOfSubsetIndividual.get(subset);
				// subtractor += varianceOfSubsetIndividual.get(subset);
			}
		}
		 System.out.println("Individual var for " + features + " = " + vU);
		if (vU < 0.0d)
			vU = 0.0d;
		varianceOfSubsetIndividual.put(features, vU);
		double fraction = vU / totalVariance;
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
	private double getMarginalPrediction(List<Integer> indices, List<Observation> observations) {
		double result = 0;
		Set<Integer> subset = new HashSet<Integer>();
		subset.addAll(indices);
		List<Double> obsList = new ArrayList<Double>(observations.size());
		for (Observation obs : observations) {
			obsList.add(obs.midPoint);
		}
		double sumOfRangeSizes = 0.0d;
		double intervalProduct = 1.0d;
		boolean consistentWithAnyLeaf = false;
		for (Tree leaf : partitioning.keySet()) {
			// if (observationConsistentWithLeaf(indices, observations, leaf)) {
			if (partitioning.get(leaf).containsPartialInstance(indices, obsList)) {
				double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfAllButSubset(subset);
				double sizeOfDomain = featureSpace.getRangeSizeOfAllButSubset(subset);
				double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
				sumOfRangeSizes += partitioning.get(leaf).getRangeSizeOfFeatureSubspace(subset);
				double prediction;
				if (leaf.getClassDistribution() != null) {
					prediction = leaf.getClassDistribution()[0];
				} else {
					System.out.println("class distribution of leaf is null!");
					return Double.NaN;
				}
				result += prediction * fractionOfSpaceForThisLeaf;
				// if (prediction == 0.0d)
				// System.out.println("Prediction is zero!");
				consistentWithAnyLeaf = true;
			}
		}
		if (!consistentWithAnyLeaf) {
			System.out.println("Observation " + obsList + " is not consistent with any leaf with indices: " + indices);
			for (Tree leaf : partitioning.keySet()) {
				// for (int index : indices)
//				for (int i = 0; i < this.getFeatureSpace().getDimensionality(); i++)
//					System.out.print(
//							"Domain " + i + ": " + partitioning.get(leaf).getFeatureDomain(i).compactString() + "\t");
//				System.out.println();
			}
		}
		return result;
	}

	/**
	 * Checks whether the observations are consistent with a leaf (for implementing
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
	 * @throws Exception
	 */
	private void computePartitioning(FeatureSpace subSpace, Tree node) {

		double splitPoint = node.getSplitPoint();
		int attribute = node.getAttribute();
		Tree[] children = node.getSuccessors();

		// boolean allChildrenEmpty = true;
		if (children != null) {
			for (Tree child : children) {
				if ((child.getClassDistribution() == null) && (child.getAttribute() == -1))
					System.out.println(node);
			}
		}
		// if node is leaf add partition to the map or
		if (attribute == -1) {
			double rangeSize = subSpace.getRangeSize();
			if (node.getClassDistribution() != null) {
				leaves.add(node);
				partitioning.put(node, subSpace);
				sizeOfPartitions.put(node, rangeSize);
			} else {
				System.out.println("Nodes class distribution is null. Corresponding Feature Space Size : "
						+ subSpace.getRangeSize());
			}
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
		ArrayList<ArrayList<Double>> splitPointsList = new ArrayList<ArrayList<Double>>(
				featureSpace.getDimensionality());
		for (int i = 0; i < featureSpace.getDimensionality(); i++) {
			splitPoints.add(i, new HashSet<Double>());
			splitPointsList.add(i, new ArrayList<Double>());
		}

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
			splitPointsList.get(node.getAttribute()).add(node.getSplitPoint());
			// Add successors to queue
			for (int i = 0; i < node.getSuccessors().length; i++) {
				queueOfNodes.add(node.getSuccessors()[i]);
			}
		}
		// System.out.println("split: ");
		// for (List<Double> points : splitPointsList) {
		// System.out.println(points);
		// }
		// System.out.println("splitpoints ende");
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
						Observation obs = new Observation(
								(curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1)) / 2,
								curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId));
						allObservations[featureIndex][lowerIntervalId] = obs;
						System.out.println(obs.midPoint);
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

	/**
	 * Computes the total variance of marginal predictions for a given set of
	 * features.
	 * 
	 * @param features
	 * @return
	 */
	private double computeTotalVarianceOfSubset(Set<Integer> features) {
		double fraction = 0;
		List<List<Observation>> observationList = new LinkedList<List<Observation>>();
		List<Set<Observation>> observationSet = new LinkedList<Set<Observation>>();
		for (int featureIndex : features) {
			List<Observation> list = Arrays.stream(allObservations[featureIndex]).collect(Collectors.toList());
			HashSet<Observation> hSet = new HashSet<Observation>();
			hSet.addAll(list);
			observationList.add(list);
			observationSet.add(hSet);
		}
		List<List<Observation>> observationProduct = Lists.cartesianProduct(observationList);
		double vU = 0.0d;
		// List<Double> marginals = new LinkedList<Double>();
		// System.out.println("size of obsprod = " + observationProduct.size());
		double weightedSum = 0, weightedSumOfSquares = 0;
		double num = 0;
		WeightedVarianceHelper stat = new WeightedVarianceHelper();
		// System.out.println("marginal predictions for: " + features);
		for (List<Observation> curObs : observationProduct) {
			ArrayList<Integer> featureList = new ArrayList<Integer>();
			featureList.addAll(features);
			Collections.sort(featureList);
			double marginalPrediction = this.getMarginalPrediction(featureList, curObs);

			// System.out.println("current feautres = \t" + features);
			// System.out.print("midpoints = \t\t");
			// for (Observation obs : curObs) {
			// System.out.print(obs.midPoint + ", ");
			// }
			// System.out.println();
			// System.out.print("interval sizes = \t");
			double prodOfIntervalSizes = 1.0d;
			for (Observation obs : curObs) {
				// System.out.print(obs.intervalSize + ", ");
				prodOfIntervalSizes *= obs.intervalSize;
			}
			// System.out.println();
			// System.out.println("marginal pred = \t" + marginalPrediction);
			double sizeOfAllButFeatures = this.getFeatureSpace().getRangeSizeOfAllButSubset(features);
			// System.out.println("sum_of_weights = \t" + sizeOfAllButFeatures);
			// System.out.println("prod of int sizes = \t" + prodOfIntervalSizes);
			// System.out.println("weight for var = \t" + prodOfIntervalSizes *
			// sizeOfAllButFeatures);
			if (!Double.isNaN(marginalPrediction))
				stat.push(marginalPrediction, sizeOfAllButFeatures * prodOfIntervalSizes);
			// weightedSum += marginalPrediction;
			// weightedSumOfSquares += marginalPrediction * marginalPrediction;
			// num++;

		}
		// weightedSumOfSquares /= num;
		// weightedSum /= num;
		// System.out.println("wsos = " + weightedSumOfSquares + "\t ws = " +
		// weightedSum);
		// vU = weightedSumOfSquares - (weightedSum * weightedSum);
		vU = stat.getVariancePopulaion();
		varianceOfSubsetTotal.put(features, vU);
		// System.out.println("Total var for \t\t\t" + features + ": " + vU);
		return vU;
	}

	public double getTotalVariance() {
		return this.totalVariance;
	}

	/**
	 * Sets up the tree for fANOVA
	 */
	public void preprocess() {
		this.computePartitioning(featureSpace, m_Tree);
		this.collectSplitPointsAndIntervalSizes(m_Tree);
		this.computeObservations();
		// this.computeTotalVariance();
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < this.featureSpace.getDimensionality(); i++) {
			set.add(i);
		}
		this.totalVariance = computeTotalVarianceOfSubset(set);
		System.out.println("trees total variance = " + this.totalVariance);
		// System.out.println("num leaves = " + leaves.size());

		// double sum = 1.0d;
		// for (int i = 0; i < allObservations.length; i++) {
		// sum *= allObservations[i].length;
		// }
	}

	public void printObservations() {
		for (int i = 0; i < allObservations.length; i++) {
			System.out.println("Observations for feature " + i + ":");
			for (int j = 0; j < allObservations[i].length; j++) {
				System.out.print(allObservations[i][j].midPoint + ", ");
			}
		}
	}

	public void printSplitPoints() {
		for (int i = 0; i < splitPoints.size(); i++) {
			Set<Double> points = splitPoints.get(i);
			List<Double> sorted = new ArrayList<Double>(points);
			if (this.getFeatureSpace().getFeatureDomain(i) instanceof NumericFeatureDomain) {
				sorted.add(((NumericFeatureDomain) this.getFeatureSpace().getFeatureDomain(i)).getMin());
				sorted.add(((NumericFeatureDomain) this.getFeatureSpace().getFeatureDomain(i)).getMax());
			}
			Collections.sort(sorted);
			System.out.println(sorted);
		}
	}

	private class WeightedVarianceHelper {
		private int numberOfSamples;
		private double average, squaredDistanceToMean, sumOfWeights;

		public WeightedVarianceHelper() {
			this.average = 0.0d;
			this.squaredDistanceToMean = 0.0d;
			this.sumOfWeights = 0.0d;
		};

		public void push(double x, double weight) {
			if (weight <= 0.0d)
				throw new IllegalArgumentException("Weights have to be strictly positive!");
			double delta = x - average;
			sumOfWeights += weight;
			average += delta * weight / sumOfWeights;
			squaredDistanceToMean += weight * delta * (x - average);
		}

		public double getVariancePopulaion() {
			if (sumOfWeights > 0.0d) {
				return Math.max(0.0d, squaredDistanceToMean / sumOfWeights);
			} else
				return Double.NaN;
		}
	}

}
