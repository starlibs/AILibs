package jaicore.ml.intervaltree;

import java.util.Arrays;

import jaicore.ml.core.Interval;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;

public class ExtendedM5Forest extends Bagging {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtendedM5Forest() {
		ExtendedM5Tree rTree = new ExtendedM5Tree();
		rTree.setDoNotCheckCapabilities(true);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(false);
		setNumIterations(defaultNumberOfIterations());
	}

	public Interval predictInterval(Instance rangeQuery) {
		// collect the different predictions
		Interval[] predictions = new Interval[this.m_Classifiers.length];
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			ExtendedM5Tree classifier = (ExtendedM5Tree) this.m_Classifiers[i];
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
