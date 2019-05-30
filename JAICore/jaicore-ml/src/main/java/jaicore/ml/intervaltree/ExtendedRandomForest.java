package jaicore.ml.intervaltree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.aggregation.QuantileAggregator;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author mirkoj, jonash
 *
 */
public class ExtendedRandomForest extends RandomForest implements RangeQueryPredictor {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 8774800172762290733L;

	private static final Logger log = LoggerFactory.getLogger(ExtendedRandomForest.class);

	private final IntervalAggregator forestAggregator;

	private FeatureSpace featureSpace;

	public ExtendedRandomForest() {
		this(new QuantileAggregator(0.15), new AggressiveAggregator());
	}

	public ExtendedRandomForest(final IntervalAggregator treeAggregator, final IntervalAggregator forestAggregator) {
		super();
		ExtendedRandomTree rTree = new ExtendedRandomTree(treeAggregator);
		this.setClassifier(rTree);
		this.forestAggregator = forestAggregator;
	}

	public ExtendedRandomForest(final FeatureSpace featureSpace) {
		this();
		this.featureSpace = featureSpace;
		ExtendedRandomTree erTree = (ExtendedRandomTree) this.getClassifier();
		erTree.setFeatureSpace(featureSpace);
	}

	public ExtendedRandomForest(final IntervalAggregator treeAggregator, final IntervalAggregator forestAggregator, final FeatureSpace featureSpace) {
		super();
		this.forestAggregator = forestAggregator;
		this.featureSpace = featureSpace;
		ExtendedRandomTree erTree = new ExtendedRandomTree(treeAggregator);
		erTree.setFeatureSpace(featureSpace);
		this.setClassifier(erTree);
	}

	/**
	 * Needs to be called before predicting marginal variance contributions!
	 *
	 * @param Instances
	 *            for which marginal variance contributions are to be estimated
	 */
	public void prepareForest(final Instances data) {
		this.featureSpace = new FeatureSpace(data);
		for (Classifier classifier : this.m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			curTree.setFeatureSpace(this.featureSpace);
			curTree.preprocess();
		}
	}

	public void printVariances() {
		for (Classifier classifier : this.m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			log.debug("cur var: {}", curTree.getTotalVariance());
		}
	}

	public double computeMarginalVarianceContributionForFeatureSubset(final Set<Integer> features) {
		double avg = 0;
		for (Classifier classifier : this.m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			double curMarg = curTree.computeMarginalVarianceContributionForSubsetOfFeatures(features);
			avg += curMarg * 1.0 / this.m_Classifiers.length;
		}
		return avg;
	}

	public double computeMarginalVarianceContributionForFeatureSubsetNotNormalized(final Set<Integer> features) {
		double avg = 0;
		for (Classifier classifier : this.m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			double curMarg = curTree.computeMarginalVarianceContributionForSubsetOfFeaturesNotNormalized(features);
			avg += curMarg * 1.0 / this.m_Classifiers.length;
		}
		return avg;
	}

	/**
	 *
	 * @return Size of
	 */
	public int getSize() {
		return this.m_Classifiers.length;
	}

	/**
	 *
	 * @return Feature space on which this forest operates on
	 */
	public FeatureSpace getFeatureSpace() {
		return this.featureSpace;
	}

	@Override
	protected String defaultClassifierString() {
		return "jaicore.ml.intervaltree.ExtendedRandomTree";
	}

	public ExtendedRandomForest(final int seed) {
		this();
		this.setSeed(seed);
	}

	@Override
	public Interval predictInterval(final Instance rangeQuery) {
		// collect the different predictions
		List<Double> predictions = new ArrayList<>(this.m_Classifiers.length * 2);
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedRandomTree classifier = (ExtendedRandomTree) this.m_Classifiers[i];
			Interval prediction = classifier.predictInterval(rangeQuery);
			predictions.add(prediction.getInf());
			predictions.add(prediction.getSup());

		}
		// aggregate them
		return this.forestAggregator.aggregate(predictions);
	}

	@Override
	public Interval predictInterval(final IntervalAndHeader intervalAndHeader) {
		// collect the different predictions
		List<Double> predictions = new ArrayList<>(this.m_Classifiers.length * 2);
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedRandomTree classifier = (ExtendedRandomTree) this.m_Classifiers[i];
			Interval prediction = classifier.predictInterval(intervalAndHeader);
			predictions.add(prediction.getInf());
			predictions.add(prediction.getSup());
		}
		// aggregate them
		return this.forestAggregator.aggregate(predictions);
	}
}