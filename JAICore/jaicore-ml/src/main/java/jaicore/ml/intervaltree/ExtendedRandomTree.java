package jaicore.ml.intervaltree;

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

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jaicore.ml.core.CategoricalFeatureDomain;
import jaicore.ml.core.FeatureDomain;
import jaicore.ml.core.FeatureSpace;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedRandomTree.class);

	private static final String LOG_WARN_VARIANCE_ZERO = "The trees total variance is zero, predictions make no sense at this point!";
	private static final String LOG_WARN_NOT_PREPARED = "Tree is not prepared, preprocessing may take a while";
	private static final String LOG_INDIVIDUAL_VAR = "Individual var for {} = {}";
	private static final String LOG_TOTAL_VAR = "current total variance for {} = {}";

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
	private transient Observation[][] allObservations;
	private HashMap<Set<Integer>, Double> varianceOfSubsetIndividual;
	private HashMap<Set<Integer>, Double> varianceOfSubsetTotal;
	private HashMap<Tree, Double> mapForEmptyLeaves;
	private boolean isPrepared;

	public ExtendedRandomTree() {
		this(new AggressiveAggregator());
		this.partitioning = new HashMap<>();
		this.leaves = new ArrayList<>();
		// important, otherwise some classdistributions may be null
		this.setAllowUnclassifiedInstances(false);
		this.varianceOfSubsetTotal = new HashMap<>();
		this.varianceOfSubsetIndividual = new HashMap<>();
		this.mapForEmptyLeaves = new HashMap<>();
		this.isPrepared = false;
	}

	public ExtendedRandomTree(final FeatureSpace featureSpace) {
		this();
		this.featureSpace = featureSpace;
		this.isPrepared = false;
	}

	public ExtendedRandomTree(final IntervalAggregator intervalAggregator) {
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
		this.varianceOfSubsetTotal = new HashMap<>();
		this.varianceOfSubsetIndividual = new HashMap<>();
		this.mapForEmptyLeaves = new HashMap<>();
		this.isPrepared = false;
	}

	@Override
	public Interval predictInterval(final IntervalAndHeader intervalAndHeader) {
		Interval[] queriedInterval = intervalAndHeader.getIntervals();
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], Tree>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(RQPHelper.getEntry(queriedInterval, this.m_Tree));

		// the list of all leaf values
		ArrayList<Double> list = new ArrayList<>();
		while (stack.peek() != null) {
			// pick the next node to process
			Entry<Interval[], Tree> toProcess = stack.pop();
			Tree nextTree = toProcess.getValue();
			double threshold = nextTree.getM_SplitPoint();
			int attribute = nextTree.getM_Attribute();
			Tree[] children = nextTree.getM_Successors();
			double[] classDistribution = nextTree.getM_Classdistribution();
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

				if (intervalForAttribute.getInf() <= threshold) {

					if (threshold <= intervalForAttribute.getSup()) {
						// scenario: x_min <= threshold <= x_max
						// query [x_min, threshold] on the left child
						// query [threshold, x_max] right
						Interval[] newInterval = RQPHelper.substituteInterval(toProcess.getKey(), new Interval(intervalForAttribute.getInf(), threshold), attribute);
						Interval[] newMaxInterval = RQPHelper.substituteInterval(toProcess.getKey(), new Interval(threshold, intervalForAttribute.getSup()), attribute);
						stack.push(RQPHelper.getEntry(newInterval, leftChild));
						stack.push(RQPHelper.getEntry(newMaxInterval, rightChild));
					} else {
						// scenario: threshold <= x_min <= x_max
						// query [x_min, x_max] on the left child
						stack.push(RQPHelper.getEntry(toProcess.getKey(), leftChild));
					}
				}
				// analogously...
				if (intervalForAttribute.getSup() > threshold) {
					stack.push(RQPHelper.getEntry(toProcess.getKey(), rightChild));
				}
			}
		}
		return this.intervalAggregator.aggregate(list);
	}

	public void setFeatureSpace(final FeatureSpace featureSpace) {
		this.featureSpace = featureSpace;
	}

	public FeatureSpace getFeatureSpace() {
		return this.featureSpace;
	}

	/**
	 * Computes the variance contribution of a subset of features.
	 *
	 * @param features
	 * @return Variance contribution of the feature subset
	 */
	public double computeMarginalStandardDeviationForSubsetOfFeatures(Set<Integer> features) {
		if (!this.isPrepared) {
			LOGGER.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}
		// as we use a set as a key, we should at least make it immutable
		features = Collections.unmodifiableSet(features);
		if (this.totalVariance == 0.0d) {
			LOGGER.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}

		double vU;
		if (this.varianceOfSubsetTotal.containsKey(features)) {
			vU = this.varianceOfSubsetTotal.get(features);
		} else {
			vU = this.computeTotalVarianceOfSubset(features);
		}
		LOGGER.trace(LOG_TOTAL_VAR, features, vU);

		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty()) {
					continue;
				}
				LOGGER.trace("Subtracting {} for {}", this.varianceOfSubsetIndividual.get(subset), subset);
				vU -= this.varianceOfSubsetIndividual.get(subset);
			}
		}
		LOGGER.trace(LOG_INDIVIDUAL_VAR, features, vU);
		if (vU < 0.0d) {
			vU = 0.0d;
		}
		this.varianceOfSubsetIndividual.put(features, vU);
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
			LOGGER.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}

		features = Collections.unmodifiableSet(features);
		if (this.totalVariance == 0.0d) {
			LOGGER.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}

		double vU;
		if (this.varianceOfSubsetTotal.containsKey(features)) {
			vU = this.varianceOfSubsetTotal.get(features);
		} else {
			vU = this.computeTotalVarianceOfSubset(features);
		}
		LOGGER.trace(LOG_TOTAL_VAR, features, vU);

		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty()) {
					continue;
				}
				LOGGER.trace("Subtracting {} for {} ", this.varianceOfSubsetIndividual.get(subset), subset);
				vU -= this.varianceOfSubsetIndividual.get(subset);
			}
		}
		LOGGER.trace(LOG_INDIVIDUAL_VAR, features, vU);
		vU = Math.max(vU, 0);
		this.varianceOfSubsetIndividual.put(features, vU);
		return vU / this.totalVariance;
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
			LOGGER.warn(LOG_WARN_NOT_PREPARED);
			this.preprocess();
		}

		features = Collections.unmodifiableSet(features);
		if (this.totalVariance == 0.0d) {
			LOGGER.warn(LOG_WARN_VARIANCE_ZERO);
			return Double.NaN;
		}

		double vU;
		if (this.varianceOfSubsetTotal.containsKey(features)) {
			vU = this.varianceOfSubsetTotal.get(features);
		} else {
			vU = this.computeTotalVarianceOfSubset(features);
		}
		LOGGER.trace(LOG_TOTAL_VAR, features, vU);

		for (int k = 1; k < features.size(); k++) {
			// generate all subsets of size k
			Set<Set<Integer>> subsets = Sets.combinations(features, k);
			for (Set<Integer> subset : subsets) {
				if (subset.isEmpty()) {
					continue;
				}
				LOGGER.trace("Subtracting {} for {} ", this.varianceOfSubsetIndividual.get(subset), subset);
				vU -= this.varianceOfSubsetIndividual.get(subset);
			}
		}
		LOGGER.trace(LOG_INDIVIDUAL_VAR, features, vU);
		if (vU < 0.0d) {
			vU = 0.0d;
		}
		this.varianceOfSubsetIndividual.put(features, vU);
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
	private double getMarginalPrediction(final List<Integer> indices, final List<Observation> observations) {
		double result = 0;
		Set<Integer> subset = new HashSet<>();
		subset.addAll(indices);
		List<Double> obsList = new ArrayList<>(observations.size());
		for (Observation obs : observations) {
			obsList.add(obs.midPoint);
		}
		boolean consistentWithAnyLeaf = false;
		for (Entry<Tree, FeatureSpace> leafEntry : this.partitioning.entrySet()) {
			Tree leaf = leafEntry.getKey();
			if (this.partitioning.get(leaf).containsPartialInstance(indices, obsList)) {
				double sizeOfLeaf = this.partitioning.get(leaf).getRangeSizeOfAllButSubset(subset);
				double sizeOfDomain = this.featureSpace.getRangeSizeOfAllButSubset(subset);
				double fractionOfSpaceForThisLeaf = sizeOfLeaf / sizeOfDomain;
				double prediction;

				if (leaf.getM_Classdistribution() != null) {
					prediction = leaf.getM_Classdistribution()[0];
				} else if (this.mapForEmptyLeaves.containsKey(leaf)) {
					prediction = this.mapForEmptyLeaves.get(leaf);
				} else {
					LOGGER.warn("No prediction found anywhere!");
					prediction = Double.NaN;
				}
				assert prediction != Double.NaN : "Prediction must not be NaN";
				result += prediction * fractionOfSpaceForThisLeaf;
				consistentWithAnyLeaf = true;
			}
		}
		if (!consistentWithAnyLeaf) {
			LOGGER.warn("Observation {} is not consistent with any leaf with indices: {}", obsList, indices);
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
	private void computePartitioning(final FeatureSpace subSpace, final Tree node) {
		double splitPoint = node.getM_SplitPoint();
		int attribute = node.getM_Attribute();
		Tree[] children = node.getM_Successors();

		// check if any child is leaf and has empty class distribution, if so add the current node
		// if node is leaf add partition to the map or
		if (attribute == -1) {
			this.leaves.add(node);
			this.partitioning.put(node, subSpace);
		}
		// if the split attribute is categorical, remove all but one from the feature space and continue
		else if (subSpace.getFeatureDomain(attribute) instanceof CategoricalFeatureDomain) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].getM_Classdistribution() == null && children[i].getM_Attribute() == -1) {
					this.mapForEmptyLeaves.put(children[i], node.getM_Classdistribution()[0]);
				}
				// important! if the children are leaves and do not contain a class distribution, treat this node as a leaf. If the split that leads
				// a leaf happens on a categorical feature, the leaf does not contain a class distribution in the WEKA RandomTree
				FeatureSpace childSubSpace = new FeatureSpace(subSpace);
				((CategoricalFeatureDomain) childSubSpace.getFeatureDomain(attribute)).setValues(new double[] { i });
				this.computePartitioning(childSubSpace, children[i]);
			}
		}
		// if the split attribute is numeric, set the new interval ranges of the resulting feature space accordingly and continue
		else if (subSpace.getFeatureDomain(attribute) instanceof NumericFeatureDomain) {
			FeatureSpace leftSubSpace = new FeatureSpace(subSpace);
			((NumericFeatureDomain) leftSubSpace.getFeatureDomain(attribute)).setMax(splitPoint);
			FeatureSpace rightSubSpace = new FeatureSpace(subSpace);
			((NumericFeatureDomain) rightSubSpace.getFeatureDomain(attribute)).setMin(splitPoint);
			this.computePartitioning(leftSubSpace, children[0]);
			this.computePartitioning(rightSubSpace, children[1]);
		}
	}

	/**
	 * Collect all split points of a given tree
	 *
	 * @param root
	 */
	private void collectSplitPointsAndIntervalSizes(final Tree root) {

		// One HashSet of split points for each feature
		this.splitPoints = new ArrayList<>(this.featureSpace.getDimensionality());
		ArrayList<ArrayList<Double>> splitPointsList = new ArrayList<>(this.featureSpace.getDimensionality());
		for (int i = 0; i < this.featureSpace.getDimensionality(); i++) {
			this.splitPoints.add(i, new HashSet<Double>());
			splitPointsList.add(i, new ArrayList<Double>());
		}

		Queue<Tree> queueOfNodes = new LinkedList<>();
		queueOfNodes.add(root);

		// While the queue is not empty
		while (!queueOfNodes.isEmpty()) {

			Tree node = queueOfNodes.poll();

			// Is node a leaf?
			if (node.getM_Attribute() <= -1) {
				continue;
			}
			this.splitPoints.get(node.getM_Attribute()).add(node.getM_SplitPoint());
			splitPointsList.get(node.getM_Attribute()).add(node.getM_SplitPoint());
			// Add successors to queue
			for (int i = 0; i < node.getM_Successors().length; i++) {
				queueOfNodes.add(node.getM_Successors()[i]);
			}
		}
	}

	/**
	 * Compute observations, i.e. representatives of each equivalence class of
	 * partitions
	 */
	private void computeObservations() {
		this.allObservations = new Observation[this.featureSpace.getDimensionality()][];
		for (int featureIndex = 0; featureIndex < this.featureSpace.getDimensionality(); featureIndex++) {
			List<Double> curSplitPoints = new ArrayList<>();
			curSplitPoints.addAll(this.splitPoints.get(featureIndex));
			// curSplitPoints
			FeatureDomain curDomain = this.featureSpace.getFeatureDomain(featureIndex);
			if (curDomain instanceof NumericFeatureDomain) {
				NumericFeatureDomain curNumDomain = (NumericFeatureDomain) curDomain;
				curSplitPoints.add(curNumDomain.getMin());
				curSplitPoints.add(curNumDomain.getMax());

				Collections.sort(curSplitPoints);
				// if the tree does not split on this value, it is not important
				if (curSplitPoints.isEmpty()) {
					this.allObservations[featureIndex] = new Observation[0];
				} else {
					this.allObservations[featureIndex] = new Observation[curSplitPoints.size() - 1];
					Observation obs;
					for (int lowerIntervalId = 0; lowerIntervalId < curSplitPoints.size() - 1; lowerIntervalId++) {
						if (curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId) > 0) {
							obs = new Observation((curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1)) / 2, curSplitPoints.get(lowerIntervalId + 1) - curSplitPoints.get(lowerIntervalId));
						} else {
							obs = new Observation((curSplitPoints.get(lowerIntervalId) + curSplitPoints.get(lowerIntervalId + 1)) / 2, 1);
						}
						this.allObservations[featureIndex][lowerIntervalId] = obs;
					}
				}
			} else if (curDomain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain cDomain = (CategoricalFeatureDomain) curDomain;
				this.allObservations[featureIndex] = new Observation[cDomain.getValues().length];
				for (int i = 0; i < this.allObservations[featureIndex].length; i++) {
					this.allObservations[featureIndex][i] = new Observation(cDomain.getValues()[i], 1.0d);
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
		if (this.varianceOfSubsetTotal.containsKey(features)) {
			return this.varianceOfSubsetTotal.get(features);
		}
		List<List<Observation>> observationList = new LinkedList<>();
		List<Set<Observation>> observationSet = new LinkedList<>();
		for (int featureIndex : features) {
			List<Observation> list = Arrays.stream(this.allObservations[featureIndex]).collect(Collectors.toList());
			HashSet<Observation> hSet = new HashSet<>();
			hSet.addAll(list);
			observationList.add(list);
			observationSet.add(hSet);
		}
		List<List<Observation>> observationProduct = Lists.cartesianProduct(observationList);
		double vU;
		WeightedVarianceHelper stat = new WeightedVarianceHelper();
		for (List<Observation> curObs : observationProduct) {
			ArrayList<Integer> featureList = new ArrayList<>();
			featureList.addAll(features);
			Collections.sort(featureList);
			double marginalPrediction = this.getMarginalPrediction(featureList, curObs);
			double prodOfIntervalSizes = 1.0d;
			for (Observation obs : curObs) {
				if (obs.intervalSize != 0) {
					prodOfIntervalSizes *= obs.intervalSize;
				}
			}
			double sizeOfAllButFeatures = this.getFeatureSpace().getRangeSizeOfAllButSubset(features);
			if (!Double.isNaN(marginalPrediction)) {
				stat.push(marginalPrediction, sizeOfAllButFeatures * prodOfIntervalSizes);

			}
		}
		vU = stat.getPopulaionVariance();
		this.varianceOfSubsetTotal.put(features, vU);
		return vU;
	}

	public double getTotalVariance() {
		return this.totalVariance;
	}

	/**
	 * Sets up the tree for fANOVA
	 */
	public void preprocess() {
		this.computePartitioning(this.featureSpace, this.m_Tree);
		this.collectSplitPointsAndIntervalSizes(this.m_Tree);
		this.computeObservations();
		HashSet<Integer> set = new HashSet<>();
		for (int i = 0; i < this.featureSpace.getDimensionality(); i++) {
			set.add(i);
		}
		this.totalVariance = this.computeTotalVarianceOfSubset(set);
		this.isPrepared = true;
	}

	public void printObservations() {
		for (int i = 0; i < this.allObservations.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < this.allObservations[i].length; j++) {
				sb.append(this.allObservations[i][j].midPoint + ", ");
			}
			LOGGER.debug("Observations for feature {}: {}", i, sb);
		}
	}

	public void printSplitPoints() {
		for (int i = 0; i < this.splitPoints.size(); i++) {
			Set<Double> points = this.splitPoints.get(i);
			List<Double> sorted = new ArrayList<>(points);
			if (this.getFeatureSpace().getFeatureDomain(i) instanceof NumericFeatureDomain) {
				sorted.add(((NumericFeatureDomain) this.getFeatureSpace().getFeatureDomain(i)).getMin());
				sorted.add(((NumericFeatureDomain) this.getFeatureSpace().getFeatureDomain(i)).getMax());
			}
			Collections.sort(sorted);
		}
	}

	public void printSizeOfFeatureSpaceAndPartitioning() {
		LOGGER.debug("Size of feature space: {}", this.featureSpace.getRangeSize());
		double sizeOfPartitioning = 0.0d;
		for (Entry<Tree, FeatureSpace> leafEntry : this.partitioning.entrySet()) {
			sizeOfPartitioning += this.partitioning.get(leafEntry.getKey()).getRangeSize();
		}
		LOGGER.debug("Complete size of partitioning: {}", sizeOfPartitioning);
		double sizeOfIntervals = 1.0d;
		for (int i = 0; i < this.allObservations.length; i++) {
			double temp = 0.0d;
			for (int j = 0; j < this.allObservations[i].length; j++) {
				temp += this.allObservations[i][j].intervalSize;
			}
			sizeOfIntervals *= temp;
		}
		LOGGER.debug("Complete size of intervals: {}", sizeOfIntervals);
	}

	/**
	 * Helper to compute weighted variances
	 *
	 * @author jmhansel
	 *
	 */
	private class WeightedVarianceHelper {
		private double average;
		private double squaredDistanceToMean;
		private double sumOfWeights;

		public WeightedVarianceHelper() {
			this.average = 0.0d;
			this.squaredDistanceToMean = 0.0d;
			this.sumOfWeights = 0.0d;
		}

		public void push(final double x, final double weight) {
			if (weight <= 0.0d) {
				throw new IllegalArgumentException("Weights have to be strictly positive!");
			}
			double delta = x - this.average;
			this.sumOfWeights += weight;
			this.average += delta * weight / this.sumOfWeights;
			this.squaredDistanceToMean += weight * delta * (x - this.average);
		}

		public double getPopulaionVariance() {
			if (this.sumOfWeights > 0.0d) {
				return Math.max(0.0d, this.squaredDistanceToMean / this.sumOfWeights);
			} else {
				return Double.NaN;
			}
		}
	}

	/**
	 * private class for dealing with observations, basically a tuple of doubles.
	 *
	 * @author jmhansel
	 *
	 */
	private class Observation {
		private double midPoint;
		private double intervalSize;

		public Observation(final double midPoint, final double intervalSize) {
			this.midPoint = midPoint;
			this.intervalSize = intervalSize;
		}
	}

}