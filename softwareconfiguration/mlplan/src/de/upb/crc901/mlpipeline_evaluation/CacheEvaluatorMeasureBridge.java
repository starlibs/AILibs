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

			if (((ReproducibleInstances) trainingData).isCacheLookup()) {
				// check in the cache if the result exists already
				Optional<Double> potentialCache = performanceDBAdapter.exists(evaluatedComponent,
						(ReproducibleInstances) trainingData);
				if (potentialCache.isPresent()) {
					System.out.println("Cache hit!");
					return potentialCache.get();
				}
			}
			System.out.println("Cache miss!");
			// query the underlying loss function
			double performance = simpleEvaluatorMeasureBridge.evaluateSplit(pl, trainingData, validationData);
			// cache it
			if (((ReproducibleInstances) trainingData).isCacheStorage()) {
				performanceDBAdapter.store(evaluatedComponent, (ReproducibleInstances) trainingData, (ReproducibleInstances)validationData, performance, simpleEvaluatorMeasureBridge.getBasicEvaluator().getClass().getName());
			}
			return performance;
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

}
