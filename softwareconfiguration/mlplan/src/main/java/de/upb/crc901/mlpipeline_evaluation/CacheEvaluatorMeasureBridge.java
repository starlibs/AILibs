package de.upb.crc901.mlpipeline_evaluation;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.ComponentInstance;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.AbstractSplitBasedClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Implements a cache for the {@link AbstractSplitBasedClassifierEvaluator}. If no cache entry is found {@link SimpleSLCSplitBasedClassifierEvaluator} is used.
 *
 * @author mirko
 *
 */
public class CacheEvaluatorMeasureBridge extends AbstractSplitBasedClassifierEvaluator<Double, Double> {

	/** Logger for controlled output. */
	private static final Logger logger = LoggerFactory.getLogger(CacheEvaluatorMeasureBridge.class);

	private ComponentInstance evaluatedComponent;

	/* Used for evaluating, when no cache entry could be found. */
	private SimpleSLCSplitBasedClassifierEvaluator simpleEvaluatorMeasureBridge;

	/* Used for looking up cache entries. */
	private PerformanceDBAdapter performanceDBAdapter;

	public CacheEvaluatorMeasureBridge(final IMeasure<Double, Double> basicEvaluator, final PerformanceDBAdapter performanceDBAdapter) {
		super(basicEvaluator);
		this.performanceDBAdapter = performanceDBAdapter;
		this.simpleEvaluatorMeasureBridge = new SimpleSLCSplitBasedClassifierEvaluator(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final Classifier pl, final Instances trainingData, final Instances validationData) throws ObjectEvaluationFailedException, InterruptedException {
		if (trainingData instanceof ReproducibleInstances) {

			if (((ReproducibleInstances) trainingData).isCacheLookup()) {
				// check in the cache if the result exists already
				Optional<Double> potentialCache = this.performanceDBAdapter.exists(this.evaluatedComponent, (ReproducibleInstances) trainingData, (ReproducibleInstances) validationData,
						this.simpleEvaluatorMeasureBridge.getBasicEvaluator().getClass().getName());
				if (potentialCache.isPresent()) {
					logger.debug("Cache hit");
					return potentialCache.get();
				}
			}
			logger.debug("Cache miss");
			// query the underlying loss function
			Instant start = Instant.now();
			double performance = this.simpleEvaluatorMeasureBridge.evaluateSplit(pl, trainingData, validationData);
			Instant end = Instant.now();
			Duration delta = Duration.between(start, end);
			// cache it
			if (((ReproducibleInstances) trainingData).isCacheStorage()) {
				this.performanceDBAdapter.store(this.evaluatedComponent, (ReproducibleInstances) trainingData, (ReproducibleInstances) validationData, performance, this.simpleEvaluatorMeasureBridge.getBasicEvaluator().getClass().getName(),
						delta.toMillis());
			}
			return performance;
		} else {
			return this.simpleEvaluatorMeasureBridge.evaluateSplit(pl, trainingData, validationData);
		}
	}

	/**
	 * Returns a lightweight copy of this object. That is, that the database
	 * connection stays established and only the component instance is updated.
	 *
	 * @param componentInstance
	 * @return the lightweight copy
	 */
	public CacheEvaluatorMeasureBridge getShallowCopy(final ComponentInstance componentInstance) {
		CacheEvaluatorMeasureBridge bridge = new CacheEvaluatorMeasureBridge(this.getBasicEvaluator(), this.performanceDBAdapter);
		bridge.evaluatedComponent = componentInstance;
		return bridge;
	}

}
