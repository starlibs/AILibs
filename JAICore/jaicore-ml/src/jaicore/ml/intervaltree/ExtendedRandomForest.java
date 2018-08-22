package jaicore.ml.intervaltree;

import java.util.Arrays;

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

	public Interval predictInterval(Instance rangeQuery) {
		// collect the different predictions
		Interval[] predictions = new Interval[this.m_Classifiers.length];
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedRandomTree classifier = (ExtendedRandomTree) this.m_Classifiers[i];
			predictions[i] = classifier.predictInterval(rangeQuery);
		}
		// aggregate them
		double avgLower = Arrays.stream(predictions).mapToDouble(Interval::getLowerBound).average()
				.orElseThrow(IllegalStateException::new);
		double avgUpper = Arrays.stream(predictions).mapToDouble(Interval::getUpperBound).average()
				.orElseThrow(IllegalStateException::new);
		return new Interval(avgLower, avgUpper);
	}
}
