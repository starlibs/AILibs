package jaicore.ml.intervaltree;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import jaicore.ml.core.Interval;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;

public class ExtendedM5Forest extends Bagging {
	
	private static double upper_percentile = 0.8;
	
	private static double lower_percentile = 1 - upper_percentile;

	private DoubleBinaryOperator forestLowerAggregationFunction;
	
	private DoubleBinaryOperator forestUpperAggregationFunction;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtendedM5Forest() {
		this(null, null);
	}
	
	public ExtendedM5Forest  (DoubleBinaryOperator forestLowerAggregationFunction, DoubleBinaryOperator forestUpperAggregationFunction) {
		ExtendedM5Tree rTree = new ExtendedM5Tree();
		rTree.setDoNotCheckCapabilities(false);
		super.setClassifier(rTree);
		super.setRepresentCopiesUsingWeights(false);
		setNumIterations(defaultNumberOfIterations());
	}

	public ExtendedM5Forest(int seed) {
		this(null, null);
		this.setSeed(seed);
		try {
			this.setOptions(new String[] {"-U"});
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
			ExtendedM5Tree classifier = (ExtendedM5Tree) this.m_Classifiers[i];
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
