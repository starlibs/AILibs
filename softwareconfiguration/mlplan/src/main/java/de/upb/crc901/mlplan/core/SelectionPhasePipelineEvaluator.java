package de.upb.crc901.mlplan.core;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.IEvaluatorMeasureBridge;
import jaicore.timing.TimedObjectEvaluator;

/**
 * Evaluator used in the selection phase of mlplan. Uses MCCV by default, but can be configured to use other Benchmarks.
 *
 * @author fmohr
 * @author jnowack
 */
public class SelectionPhasePipelineEvaluator extends TimedObjectEvaluator<ComponentInstance, Double> implements IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SelectionPhasePipelineEvaluator.class);

	private final PipelineEvaluatorBuilder config;

	private Double bestScore;

	public SelectionPhasePipelineEvaluator(final PipelineEvaluatorBuilder config) {
		super();
		this.config = config;
	}

	@Override
	public String getLoggerName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public Double evaluateSupervised(final ComponentInstance c) throws InterruptedException, ObjectEvaluationFailedException {

		if (this.bestScore == null) {
			throw new UnsupportedOperationException("Cannot evaluated in selection phase if no best solution has been propagated.");
		}
		IEvaluatorMeasureBridge<Double> bridge = this.config.getEvaluationMeasurementBridge();
		if (this.config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
			bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);
		}

		this.logger.debug("Running probabilistic MCCV with {} iterations and best score {}", this.config.getNumMCIterations(), this.bestScore);
		ProbabilisticMonteCarloCrossValidationEvaluator mccv = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.bestScore, this.config.getData(),
				this.config.getTrainFoldSize(), this.config.getSeed());

		DescriptiveStatistics stats = new DescriptiveStatistics();
		try {
			mccv.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c), stats);
		} catch (InterruptedException e) {
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}

		/* now retrieve .75-percentile from stats */
		double mean = stats.getMean();
		double percentile = stats.getPercentile(75f);
		this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, stats.getN());
		return percentile;
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		if (bestScore == null) {
			throw new IllegalArgumentException("Best known score must not be updated with NULL");
		}
		this.bestScore = bestScore;
	}

	@Override
	public long getTimeout(ComponentInstance item) {
		return config.getTimeoutForSolutionEvaluation();
	}

	@Override
	public String getMessage(ComponentInstance item) {
		return "Pipeline evaluation during selection phase for candidate " + item;
	}

}
