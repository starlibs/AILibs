package jaicore.ml.evaluation.evaluators.weka;

import jaicore.ml.evaluation.measures.IMeasure;
import weka.classifiers.Classifier;
import weka.core.Instances;
/**
 * An abstract evaluator measure-bridge 
 * @author elppa
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
