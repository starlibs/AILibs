package jaicore.ml.intervaltree;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.aggregation.AggressiveAggregator;
import jaicore.ml.intervaltree.aggregation.IntervalAggregator;
import jaicore.ml.intervaltree.aggregation.QuantileAggregator;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;

/**
 * 
 * @author elppa
 *
 */
public class ExtendedRandomForest extends RandomForest {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 8774800172762290733L;
	
	private final IntervalAggregator forestAggregator;


	public ExtendedRandomForest() {
		this(new QuantileAggregator(0.15), new AggressiveAggregator());
	}
	
	public ExtendedRandomForest(IntervalAggregator treeAggregator, IntervalAggregator forestAggregator) {
		ExtendedRandomTree rTree = new ExtendedRandomTree(treeAggregator);
		rTree.setDoNotCheckCapabilities(true);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(true);
		setNumIterations(defaultNumberOfIterations());
		this.forestAggregator = forestAggregator;
		try {
			this.setOptions(new String[] {"-U"});
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't unprune the forest.");
		}
	}
	
	@Override
	protected String defaultClassifierString() {
		return "jaicore.ml.intervaltree.ExtendedRandomTree";
	}

	public ExtendedRandomForest(int seed) {
		this();
		this.setSeed(seed);
	}

	public Interval predictInterval(Instance rangeQuery) {
		// collect the different predictions
		List<Double> predictions = new ArrayList<>(m_Classifiers.length * 2);
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedRandomTree classifier = (ExtendedRandomTree) this.m_Classifiers[i];
			Interval prediction = classifier.predictInterval(rangeQuery);
			predictions.add(prediction.getLowerBound());
			predictions.add(prediction.getUpperBound());

		}
		// aggregate them
		return forestAggregator.aggregate(predictions);
	}
}
