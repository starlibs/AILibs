package jaicore.ml.intervaltree;

import static org.junit.Assert.assertTrue;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jaicore.ml.core.CategoricalFeatureDomain;
import jaicore.ml.core.FeatureDomain;
import jaicore.ml.core.FeatureSpace;
import jaicore.ml.core.Interval;
import jaicore.ml.core.NumericFeatureDomain;
import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.util.RQPHelper;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.classifiers.trees.RandomTree;

/**
 * Extension of a classic RandomTree to predict intervals. This class also
 * provides an implementaion of fANOVA based on Hutter et al.s implementation
 * https://github.com/frank-hutter/fanova
 * 
 * @author mirkoj
 *
 */
public class ExtendedRandomTree extends RandomTree implements RangeQueryPredictor {

	private static final String LOG_WARN_VARIANCE_ZERO = "The trees total variance is zero, predictions make no sense at this point!";

	private static final String LOG_WARN_NOT_PREPARED = "Tree is not prepared, preprocessing may take a while";

	/**
	 * 
	 * For serialization purposes
	 */
	private static final long serialVersionUID = -467555221387281335L;

	private final IntervalAggregator intervalAggregator;
	private FeatureSpace featureSpace;
	private HashMap<Tree, FeatureSpace> partitioning;
	private ArrayList<Tree> leaves;
	private ArrayList<Set<Double>> splitPoints;
	private double totalVariance;
	private Observation[][] allObservations;
	private HashMap<Set<Integer>, Double> varianceOfSubsetIndividual;
	private HashMap<Set<Integer>, Double> varianceOfSubsetTotal;
	private HashMap<Tree, Double> mapForEmptyLeaves;
	private boolean isPrepared;

	private static final Logger log = LoggerFactory.getLogger(ExtendedRandomTree.class);

	public ExtendedRandomTree() {
		this(new AggressiveAggregator());
		this.partitioning = new HashMap<>();
		this.leaves = new ArrayList<>();
		// important, otherwise some classdistributions may be null
		this.setAllowUnclassifiedInstances(false);
		varianceOfSubsetTotal = new HashMap<>();
		varianceOfSubsetIndividual = new HashMap<>();
		mapForEmptyLeaves = new HashMap<>();
		this.isPrepared = false;
	}

	public ExtendedRandomTree(FeatureSpace featureSpace) {
		this();
		this.featureSpace = featureSpace;
		this.isPrepared = false;
	}

	public ExtendedRandomTree(IntervalAggregator intervalAggregator) {
		super();
		try {
			this.setOptions(new String[] { "-U" });
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't unprune the tree");
		}
		this.intervalAggregator = intervalAggregator;
		this.partitioning = new HashMap<>();
		this.leaves = new ArrayList<>();
		// important, otherwise some classdistributions may be null
		this.setAllowUnclassifiedInstances(false);
		varianceOfSubsetTotal = new HashMap<>();
		varianceOfSubsetIndividual = new HashMap<>();
		mapForEmptyLeaves = new HashMap<>();
		this.isPrepared = false;
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
	 * Computes the variance contribution of a subset of features.
	 * 
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalStandardDeviationForSubsetOfFeatures(Set<Integer> features) {
		if (!this.isPrepared) {
			log.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}
		// as we use a set as a key, we should at least make it immutable
		features = Collections.unmodifiableSet(features);
		double vU;
		if (this.totalVariance == 0.0d) {
			log.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}
		if (varianceOfSubsetTotal.containsKey(features)) {
			vU = varianceOfSubsetTotal.get(features);
		} else {
			vU = computeTotalVarianceOfSubset(features);
		}
		log.trace("current total variance for {} = {}", features, vU);
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty()) {
					continue;
				}
				log.trace("Subtracting {} for {}", varianceOfSubsetIndividual.get(subset), subset);
				vU -= varianceOfSubsetIndividual.get(subset);
				// subtractor += varianceOfSubsetIndividual.get(subset);
			}
		}
		log.trace("Individual var for {} = {}", features, vU);
		if (vU < 0.0d)
			vU = 0.0d;
		varianceOfSubsetIndividual.put(features, vU);
		return Math.sqrt(vU);
	}

	/**
	 * Computes the variance contribution of a subset of features.
	 * 
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalVarianceContributionForSubsetOfFeatures(Set<Integer> features) {
		if (!this.isPrepared) {
			log.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}
		features = Collections.unmodifiableSet(features);
		double vU;
		if (this.totalVariance == 0.0d) {
			log.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}
		if (varianceOfSubsetTotal.containsKey(features))
			vU = varianceOfSubsetTotal.get(features);
		else
			vU = computeTotalVarianceOfSubset(features);
		log.trace("current total variance for {} = {}", features, vU);
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty())
					continue;
				log.trace("Subtracting {} for {} ", varianceOfSubsetIndividual.get(subset), subset);
				vU -= varianceOfSubsetIndividual.get(subset);
				// subtractor += varianceOfSubsetIndividual.get(subset);
			}
		}
		log.trace("Individual var for {} = {}", features, vU);
		if (vU < 0.0d)
			vU = 0.0d;
		varianceOfSubsetIndividual.put(features, vU);
		return vU / totalVariance;
	}

	/**
	 * Computes the variance contribution of a subset of features without
	 * normalizing.
	 * 
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalVarianceContributionForSubsetOfFeaturesNotNormalized(Set<Integer> features) {
		if (!this.isPrepared) {
			log.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}
		features = Collections.unmodifiableSet(features);
		double vU;
		if (this.totalVariance == 0.0d) {
			log.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}
		if (varianceOfSubsetTotal.containsKey(features))
			vU = varianceOfSubsetTotal.get(features);
		else
			vU = computeTotalVarianceOfSubset(features);
		log.trace("current total variance for {} = {}", features, vU);
		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty())
					continue;
				log.trace("Subtracting {} for {} ", varianceOfSubsetIndividual.get(subset), subset);
				vU -= varianceOfSubsetIndividual.get(subset);
				// subtractor += varianceOfSubsetIndividual.get(subset);
			}
		}
		log.trace("Individual var for {} = {}", features, vU);
		if (vU < 0.0d)
			vU = 0.0d;
		varianceOfSubsetIndividual.put(features, vU);
		return vU;
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
		Set<Integer> subset = new HashSet<>();
		subset.addAll(indices);
		List<Double> obsList = new ArrayList<>(observations.size());
		for (Observation obs : observations) {
			obsList.add(obs.midPoint);
		}
		double sumOfRangeSizes = 0.0d;
		boolean consistentWithAnyLeaf = false;
		for (Tree leaf : partitioning.keySet()) {
			if (partitioning.get(leaf).containsPartialInstance(indices, obsList)) {
				double sizeOfLeaf = partitioning.get(leaf).getRangeSizeOfAllButSubset(subset);
				double sizeOfDomain = featureSpace.getRangeSizeOfAllButSubset(subset);
				double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
				sumOfRangeSizes += partitioning.get(leaf).getRangeSizeOfFeatureSubspace(subset);
				double prediction;

				if (leaf.getClassDistribution() != null) {
					prediction = leaf.getClassDistribution()[0];
				} else if (mapForEmptyLeaves.containsKey(leaf)) {
					prediction = mapForEmptyLeaves.get(leaf);
					// System.out.println("Taking prediction " + prediction + " from map because
					// distribution is null!");
				} else {
					System.out.println("No prediction found anywhere!");
					prediction = Double.NaN;
				}
				assertTrue(prediction != Double.NaN);
				result += prediction * fractionOfSpaceForThisLeaf;
				consistentWithAnyLeaf = true;
			}
		}
		if (!consistentWithAnyLeaf) {
			System.out.println("Observation " + obsList + " is not consistent with any leaf with indices: " + indices);
		}
		return result;
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

		// check if any child is leaf and has empty class distribution, if so add
		// the current node

		// if node is leaf add partition to the map or
		if (attribute == -1) {
			this.leaves.add(node);
			this.partitioning.put(node, subSpace);
			return;
		}
		// if the split attribute is categorical, remove all but one from the
		// feature space and continue
		else if (subSpace.getFeatureDomain(attribute) instanceof CategoricalFeatureDomain) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].getClassDistribution() == null && children[i].getAttribute() == -1) {
					mapForEmptyLeaves.put(children[i], node.getClassDistribution()[0]);
				}
				// important! if the children are leaves and do not contain a class
				// distribution, treat this node as a leaf. If the split that leads
				// a leaf happens on a categorical feature, the leaf does not contain
				// a class distribution in the WEKA RandomTree
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
	 * Collect all split points of a given tree
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
					Observation obs;
					for (int lowerIntervalId = 0; lowerIntervalId < curSplitPoints.size() - 1; lowerIntervalId++) {
						if (curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId) > 0)
							obs = new Observation(
									(curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1)) / 2,
									curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId));
						else {
							// TODO this is a workaround for intervals of size 0 (which should not appear
							// anyways)
							obs = new Observation(
									(curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1)) / 2,
									1);
						}
						allObservations[featureIndex][lowerIntervalId] = obs;
					}
				}
			} else if (curDomain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain cDomain = (CategoricalFeatureDomain) curDomain;
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
	public double computeTotalVarianceOfSubset(Set<Integer> features) {
		features = Collections.unmodifiableSet(features);
		if (varianceOfSubsetTotal.containsKey(features))
			return varianceOfSubsetTotal.get(features);
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
		WeightedVarianceHelper stat = new WeightedVarianceHelper();
		// System.out.println("marginal predictions for: " + features);
		for (List<Observation> curObs : observationProduct) {
			ArrayList<Integer> featureList = new ArrayList<Integer>();
			featureList.addAll(features);
			Collections.sort(featureList);
			double marginalPrediction = this.getMarginalPrediction(featureList, curObs);
			// System.out.println(marginalPrediction + ",");
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
				if (obs.intervalSize != 0)
					prodOfIntervalSizes *= obs.intervalSize;
			}
			// System.out.println();
			// System.out.println("marginal pred = \t" + marginalPrediction);
			double sizeOfAllButFeatures = this.getFeatureSpace().getRangeSizeOfAllButSubset(features);
			// System.out.println("sum_of_weights = \t" + sizeOfAllButFeatures);
			// System.out.println("prod of int sizes = \t" + prodOfIntervalSizes);
			// System.out.println("weight for var = \t" + prodOfIntervalSizes *
			// sizeOfAllButFeatures);
			if (!Double.isNaN(marginalPrediction)) {
				// System.out.println("Size of all but features: " + sizeOfAllButFeatures);
				// System.out.println("Prod of interval sizes: " + prodOfIntervalSizes);
				stat.push(marginalPrediction, sizeOfAllButFeatures * prodOfIntervalSizes);

			}
			// weightedSum += marginalPrediction;
			// weightedSumOfSquares += marginalPrediction * marginalPrediction;
			// num++;
		}
		// weightedSumOfSquares /= num;
		// weightedSum /= num;
		// System.out.println("wsos = " + weightedSumOfSquares + "\t ws = " +
		// weightedSum);
		// vU = weightedSumOfSquares - (weightedSum * weightedSum);
		vU = stat.getPopulaionVariance();
		// System.out.println("Variance: " + vU);
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
		// System.out.println("Range size: " + this.featureSpace.getRangeSize());
		this.totalVariance = computeTotalVarianceOfSubset(set);
		// System.out.println("trees total variance = " + this.totalVariance);
		this.isPrepared = true;
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

	public void printSizeOfFeatureSpaceAndPartitioning() {
		System.out.println("Size of feature space: " + this.featureSpace.getRangeSize());
		double sizeOfPartitioning = 0.0d;
		for (Tree leaf : partitioning.keySet()) {
			sizeOfPartitioning += partitioning.get(leaf).getRangeSize();
		}
		System.out.println("Complete size of partitioning: " + sizeOfPartitioning);
		double sizeOfIntervals = 1.0d;
		for (int i = 0; i < allObservations.length; i++) {
			double temp = 0.0d;
			for (int j = 0; j < allObservations[i].length; j++)
				temp += allObservations[i][j].intervalSize;
			sizeOfIntervals *= temp;
		}
		System.out.println("Complete size of intervals: " + sizeOfIntervals);
	}

	/**
	 * Helper to compute weighted variances
	 * 
	 * @author jmhansel
	 *
	 */
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

		public double getPopulaionVariance() {
			if (sumOfWeights > 0.0d) {
				return Math.max(0.0d, squaredDistanceToMean / sumOfWeights);
			} else
				return Double.NaN;
		}
	}

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
}
