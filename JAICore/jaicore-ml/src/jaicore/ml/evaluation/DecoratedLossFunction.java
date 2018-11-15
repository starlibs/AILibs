package jaicore.ml.evaluation;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class DecoratedLossFunction implements BasicMLEvaluator{
	
	private BasicMLEvaluator decoratedLossFunction;

	public DecoratedLossFunction (BasicMLEvaluator toDecorate) {
		this.decoratedLossFunction = toDecorate;
	}

	@Override
	public double getErrorRateForRandomSplit(Classifier c, Instances data, double splitSize) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * The split has to be done by {@link WekaUtil#getStratifiedSplit(Instances, java.util.Random, double...)}
	 * 
	 */
	@Override
	public double getErrorRateForSplit(Classifier c, Instances train, Instances test) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
