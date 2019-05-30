package jaicore.ml.intervaltree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.aggregation.QuantileAggregator;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;

public class ExtendedM5Forest extends Bagging implements RangeQueryPredictor {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 8774800172762290733L;

	private final IntervalAggregator forestAggregator;

	public ExtendedM5Forest() {
		this(new QuantileAggregator(0.15), new AggressiveAggregator());
	}

	public ExtendedM5Forest(final IntervalAggregator treeAggregator, final IntervalAggregator forestAggregator) {
		ExtendedM5Tree rTree = new ExtendedM5Tree(treeAggregator);
		rTree.setDoNotCheckCapabilities(false);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(false);
		this.setNumIterations(this.defaultNumberOfIterations());
		this.forestAggregator = forestAggregator;
	}

	public ExtendedM5Forest(final int seed) {
		this();
		this.setSeed(seed);
	}

	@Override
	protected String defaultClassifierString() {
		return "jaicore.ml.intervaltree.ExtendedM5Tree";
	}

	@Override
	public Interval predictInterval(final Instance rangeQuery) {
		// collect the different predictions
		List<Double> predictions = new ArrayList<>(this.m_Classifiers.length * 2);
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedM5Tree classifier = (ExtendedM5Tree) this.m_Classifiers[i];
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
			ExtendedM5Tree classifier = (ExtendedM5Tree) this.m_Classifiers[i];
			Interval prediction = classifier.predictInterval(intervalAndHeader);
			predictions.add(prediction.getInf());
			predictions.add(prediction.getSup());

		}
		// aggregate them
		return this.forestAggregator.aggregate(predictions);
	}
}
