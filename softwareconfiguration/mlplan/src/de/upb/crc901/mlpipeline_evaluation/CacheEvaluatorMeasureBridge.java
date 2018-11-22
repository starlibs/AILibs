package de.upb.crc901.mlpipeline_evaluation;

import java.util.Optional;

import hasco.model.ComponentInstance;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.SimpleEvaluatorMeasureBridge;
import jaicore.ml.evaluation.measures.IMeasure;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class CacheEvaluatorMeasureBridge extends AbstractEvaluatorMeasureBridge<Double, Double> {
	public enum BENCHMARK_TYPES {SELECTION_BENCHMARK, SEARCH_BENCHMARK};

	
	private BENCHMARK_TYPES type; 
	
	ComponentInstance evaluatedComponent;

	/* Used for evaluating, when no cache entry could be found. */
	SimpleEvaluatorMeasureBridge simpleEvaluatorMeasureBridge;

	/* Used for looking up cache entries. */
	PerformanceDBAdapter performanceDBAdapter;

	public CacheEvaluatorMeasureBridge(IMeasure<Double, Double> basicEvaluator,
			PerformanceDBAdapter performanceDBAdapter) {
		super(basicEvaluator);
		this.performanceDBAdapter = performanceDBAdapter;
		this.simpleEvaluatorMeasureBridge = new SimpleEvaluatorMeasureBridge(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(Classifier pl, Instances trainingData, Instances validationData) throws Exception {
		if (trainingData instanceof ReproducibleInstances) {
			// check in the cache if the result exists already
			Optional<Double> potentialCache = performanceDBAdapter.exists(evaluatedComponent,
					(ReproducibleInstances) trainingData);
			if (potentialCache.isPresent()) {
				System.out.println("Cache entry found."+ type);
				return potentialCache.get();
			} else {
				// query the underlying loss function
				System.out.println("No Cache Entry found." +type);
				double performance = simpleEvaluatorMeasureBridge.evaluateSplit(pl, trainingData, validationData);
				if (performance == Double.NaN) {
					performance = 0.0;
				}
				// cache it
				performanceDBAdapter.store(evaluatedComponent, (ReproducibleInstances) trainingData, performance);
				return performance;
			}
		} else {
			return simpleEvaluatorMeasureBridge.evaluateSplit(pl, trainingData, validationData);
		}
	}

	/**
	 * Returns a lightweight copy of this object. That is, that the database
	 * connection stays established and only the component instace is updated.
	 * 
	 * @param componentInstance
	 * @return
	 */
	public CacheEvaluatorMeasureBridge getShallowCopy(ComponentInstance componentInstance) {
		CacheEvaluatorMeasureBridge bridge = new CacheEvaluatorMeasureBridge(this.basicEvaluator,
				this.performanceDBAdapter);
		bridge.evaluatedComponent = componentInstance;
		return bridge;
	}

	
	public void setBenchmarkType (BENCHMARK_TYPES type) {
		this.type = type;
	}
}
