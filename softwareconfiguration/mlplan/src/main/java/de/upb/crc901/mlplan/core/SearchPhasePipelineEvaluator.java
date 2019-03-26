package de.upb.crc901.mlplan.core;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlpipeline_evaluation.SimpleUploaderMeasureBridge;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SearchPhasePipelineEvaluator implements IObjectEvaluator<ComponentInstance, Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SearchPhasePipelineEvaluator.class);
	
	private final ClassifierFactory classifierFactory;
	private final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;
	private final int seed;
	private final int numMCIterations;
	private final Instances dataShownToSearch;
	private final double trainFoldSize;
	private final IObjectEvaluator<Classifier, Double> searchBenchmark;
	

	public SearchPhasePipelineEvaluator(ClassifierFactory classifierFactory, AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge, int numMCIterations, Instances dataShownToSearch, double trainFoldSize, int seed) {
		super();
		this.classifierFactory = classifierFactory;
		this.evaluationMeasurementBridge = evaluationMeasurementBridge;
		this.seed = seed;
		this.dataShownToSearch = dataShownToSearch;
		this.numMCIterations = numMCIterations;
		this.trainFoldSize = trainFoldSize;
		this.searchBenchmark = new MonteCarloCrossValidationEvaluator(this.evaluationMeasurementBridge, numMCIterations, dataShownToSearch, trainFoldSize, seed);
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger name from {} to {}", logger.getName(), name);
		logger = LoggerFactory.getLogger(name);
		if (searchBenchmark instanceof ILoggingCustomizable) {
			logger.info("Setting logger name of actual benchmark {} to {}", searchBenchmark.getClass().getName(), name + ".benchmark");
			((ILoggingCustomizable) searchBenchmark).setLoggerName(name + ".benchmark");
		}
		else
			logger.info("Benchmark {} does not implement ILoggingCustomizable, not customizing its logger.", searchBenchmark.getClass().getName());
	}

	@Override
	public Double evaluate(ComponentInstance c) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException {
		try {
			if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
				CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) this.evaluationMeasurementBridge).getShallowCopy(c);
				long seed = this.seed + c.hashCode();
				IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new MonteCarloCrossValidationEvaluator(bridge, numMCIterations, this.dataShownToSearch,
						trainFoldSize, seed);

				return copiedSearchBenchmark.evaluate(classifierFactory.getComponentInstantiation(c));
			} else if (this.evaluationMeasurementBridge instanceof SimpleUploaderMeasureBridge) {
				SimpleUploaderMeasureBridge bridge = (SimpleUploaderMeasureBridge) evaluationMeasurementBridge;
				long start = System.currentTimeMillis();
				Classifier classifier = classifierFactory.getComponentInstantiation(c);
				double result = 0;
				try {
					result = searchBenchmark.evaluate(classifier);
				} catch(ObjectEvaluationFailedException e) {
					bridge.receiveFinalResult(classifier, 1, "Search", System.currentTimeMillis()-start);
					throw e;
				}
				
				bridge.receiveFinalResult(classifier, result, "Search", System.currentTimeMillis()-start);
				return result;
			}
			return searchBenchmark.evaluate(classifierFactory.getComponentInstantiation(c));
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
		}
	}

}
