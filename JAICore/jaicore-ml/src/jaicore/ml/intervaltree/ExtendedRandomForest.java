package jaicore.ml.intervaltree;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.RQPHelper.TREE_AGGREGATION;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;

/**
 * 
 * @author elppa
 *
 */
public class ExtendedRandomForest extends RandomForest {
	
	private static double upper_percentile = 0.2;
	
	private static double lower_percentile = 1 - upper_percentile;

	private final TREE_AGGREGATION lowerBoundAggregation;

	private final TREE_AGGREGATION upperAggregation;

	private static final long serialVersionUID = 1L;

	public ExtendedRandomForest(TREE_AGGREGATION lowerBoundAggregation, TREE_AGGREGATION upperBoundAggregation) {
		this.lowerBoundAggregation = lowerBoundAggregation;
		this.upperAggregation = upperBoundAggregation;
		ExtendedRandomTree rTree = new ExtendedRandomTree();
		rTree.setDoNotCheckCapabilities(true);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(true);
		setNumIterations(defaultNumberOfIterations());
	}

	public ExtendedRandomForest() {
		this(TREE_AGGREGATION.AVERAGE, TREE_AGGREGATION.AVERAGE);
	}
	@Override
	protected String defaultClassifierString() {
		return "jaicore.ml.intervaltree.ExtendedRandomTree";
	}

	public ExtendedRandomForest(int seed) {
		this(TREE_AGGREGATION.AVERAGE, TREE_AGGREGATION.AVERAGE);
		this.setSeed(seed);
		try {
			this.setOptions(new String[] {"-M", "6"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Interval predictInterval(Instance rangeQuery) {
		// collect the different predictions
		Interval[] predictions = new Interval[this.m_Classifiers.length];
		double [] lowers = new double [this.m_Classifiers.length];
		double [] uppers = new double [this.m_Classifiers.length];
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedRandomTree classifier = (ExtendedRandomTree) this.m_Classifiers[i];
			predictions[i] = classifier.predictInterval(rangeQuery);
			lowers [i] = predictions[i].getLowerBound();
			uppers[i] = predictions[i].getUpperBound();
		}
		Percentile perctl = new Percentile();
		double lower = perctl.evaluate(lowers, lower_percentile);
		double upper = perctl.evaluate(uppers, upper_percentile);
		return new Interval(lower, upper);
	}
}
