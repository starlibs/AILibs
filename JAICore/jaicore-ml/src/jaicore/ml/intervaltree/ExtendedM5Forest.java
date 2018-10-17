package jaicore.ml.intervaltree;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.aggregation.QuantileAggregator;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;

public class ExtendedM5Forest extends Bagging {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 8774800172762290733L;
	
	private final IntervalAggregator forestAggregator;

	public ExtendedM5Forest() {
		this(new QuantileAggregator(0.15), new AggressiveAggregator());
	}

	public ExtendedM5Forest(IntervalAggregator treeAggregator, IntervalAggregator forestAggregator) {
		ExtendedM5Tree rTree = new ExtendedM5Tree(treeAggregator);
		rTree.setDoNotCheckCapabilities(false);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(false);
		setNumIterations(defaultNumberOfIterations());
		this.forestAggregator = forestAggregator;
		try {
			this.setOptions(new String[] { "-U" });
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't unprune the forest.");
		}
	}

	public ExtendedM5Forest(int seed) {
		this();
		this.setSeed(seed);
	}
	
	@Override
	protected String defaultClassifierString() {
		return "jaicore.ml.intervaltree.ExtendedM5Tree";
	}

	public Interval predictInterval(Instance rangeQuery) {
		// collect the different predictions
		List<Double> predictions = new ArrayList<>(m_Classifiers.length * 2);
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedM5Tree classifier = (ExtendedM5Tree) this.m_Classifiers[i];
			Interval prediction = classifier.predictInterval(rangeQuery);
			predictions.add(prediction.getLowerBound());
			predictions.add(prediction.getUpperBound());

		}
		// aggregate them
		return forestAggregator.aggregate(predictions);
	}
}
