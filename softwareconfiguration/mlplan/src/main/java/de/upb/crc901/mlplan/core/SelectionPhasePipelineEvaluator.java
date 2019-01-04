package de.upb.crc901.mlplan.core;

import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import weka.core.Instances;

public class SelectionPhasePipelineEvaluator implements IObjectEvaluator<ComponentInstance, Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SelectionPhasePipelineEvaluator.class);
	
	private final ClassifierFactory classifierFactory;
	private final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;
	
	private final int seed;
	private final int numMCIterations;
	private final Instances dataShownToSelectionPhase;
	private final double trainFoldSize;

	public SelectionPhasePipelineEvaluator(ClassifierFactory classifierFactory, AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge, int numMCIterations, Instances dataShownToSearch, double trainFoldSize, int seed) {
		super();
		this.classifierFactory = classifierFactory;
		this.evaluationMeasurementBridge = evaluationMeasurementBridge;
		this.seed = seed;
		this.numMCIterations = numMCIterations;
		this.dataShownToSelectionPhase = dataShownToSearch;
		this.trainFoldSize = trainFoldSize;
	}

	@Override
	public String getLoggerName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoggerName(String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public Double evaluate(ComponentInstance c) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException {
		
		AbstractEvaluatorMeasureBridge<Double, Double> bridge = this.evaluationMeasurementBridge;
		if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
			bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);

		}

		MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(bridge,
				numMCIterations, dataShownToSelectionPhase,
				trainFoldSize, seed);
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		try {
			
			mccv.evaluate(classifierFactory.getComponentInstantiation(c), stats);
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException(e,
					"Evaluation of composition failed as the component instantiation could not be built.");
		}
		
		/* now retrieve .75-percentile from stats */
		double mean = stats.getMean();
		double percentile = stats.getPercentile(75f);
		logger.info(
				"Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}",
				percentile, mean, stats.getN());
		return percentile;
	}

}
