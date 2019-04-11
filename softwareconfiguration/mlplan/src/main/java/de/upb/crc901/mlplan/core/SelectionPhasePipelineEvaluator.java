package de.upb.crc901.mlplan.core;

import java.util.TimerTask;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.interrupt.Interrupter;
import jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.IEvaluatorMeasureBridge;

/**
 * Evaluator used in the selection phase of mlplan. Uses MCCV by default, but can be configured to use other Benchmarks.
 *
 * @author fmohr
 * @author jnowack
 */
public class SelectionPhasePipelineEvaluator implements IObjectEvaluator<ComponentInstance, Double>, IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

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
	public Double evaluate(final ComponentInstance c) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
		IEvaluatorMeasureBridge<Double> bridge = this.config.getEvaluationMeasurementBridge();
		if (this.config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
			bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);
		}

		ProbabilisticMonteCarloCrossValidationEvaluator mccv = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.bestScore, this.config.getData(),
				this.config.getTrainFoldSize(), this.config.getSeed());

		DescriptiveStatistics stats = new DescriptiveStatistics();
		TimeoutSubmitter sub = TimeoutTimer.getInstance().getSubmitter();
		TimerTask task = sub.interruptMeAfterMS(this.config.getTimeoutForSolutionEvaluation() - 100, "Timeout for pipeline in selection phase for candidate " + c + ".");
		try {
			mccv.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c), stats);
		} catch (InterruptedException e) {
			if (Interrupter.get().hasCurrentThreadBeenInterruptedWithReason(task)) {
				throw new ObjectEvaluationFailedException("Evaluation of composition failed since the timeout was hit.", e);
			}
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		} finally {
			task.cancel();
			this.logger.debug("Canceled timeout job {}", task);
		}

		/* now retrieve .75-percentile from stats */
		double mean = stats.getMean();
		double percentile = stats.getPercentile(75f);
		this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, stats.getN());
		return percentile;
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

}
