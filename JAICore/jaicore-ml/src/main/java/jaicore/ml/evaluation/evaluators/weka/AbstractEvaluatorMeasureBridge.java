package jaicore.ml.evaluation.evaluators.weka;

import jaicore.ml.core.evaluation.measure.IMeasure;
import weka.classifiers.Classifier;
import weka.core.Instances;
/**
 * Connection between an Evaluator (e.g. MCC) and a loss Function. Able to evaluate instances based on training data and validation data.
 * This bridge may modify this process, for example by using a cache.
 *  
 * @author mirko
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public abstract class AbstractEvaluatorMeasureBridge <I, O>{
	
	
	protected final IMeasure<I, O> basicEvaluator;

	
	public AbstractEvaluatorMeasureBridge(final IMeasure<I, O> basicEvaluator) {
		this.basicEvaluator = basicEvaluator;
	}
	
	public abstract O evaluateSplit(final Classifier pl, Instances trainingData, Instances validationData) throws Exception;

	public IMeasure<I, O> getBasicEvaluator(){
		return basicEvaluator;
	}
}
