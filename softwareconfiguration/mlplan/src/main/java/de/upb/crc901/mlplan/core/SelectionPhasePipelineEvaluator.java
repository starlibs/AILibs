package de.upb.crc901.mlplan.core;

import java.util.TimerTask;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.interrupt.Interrupter;
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
	private final int timeoutForSolutionEvaluation;

	public SelectionPhasePipelineEvaluator(final ClassifierFactory classifierFactory, final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge, final int numMCIterations, final Instances dataShownToSearch,
			final double trainFoldSize, final int seed, final int timeoutForSolutionEvaluation) {
		super();
		this.classifierFactory = classifierFactory;
		this.evaluationMeasurementBridge = evaluationMeasurementBridge;
		this.seed = seed;
		this.numMCIterations = numMCIterations;
		this.dataShownToSelectionPhase = dataShownToSearch;
		this.trainFoldSize = trainFoldSize;
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
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

		AbstractEvaluatorMeasureBridge<Double, Double> bridge = this.evaluationMeasurementBridge;
		if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
			bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);
		}

		MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(bridge, this.numMCIterations, this.dataShownToSelectionPhase, this.trainFoldSize, this.seed);

		DescriptiveStatistics stats = new DescriptiveStatistics();
		TimeoutSubmitter sub = TimeoutTimer.getInstance().getSubmitter();
		TimerTask task = sub.interruptMeAfterMS(this.timeoutForSolutionEvaluation - 100, "Timeout for pipeline in selection phase for candidate " + c + ".");
		try {
			mccv.evaluate(this.classifierFactory.getComponentInstantiation(c), stats);
		} catch (InterruptedException e) {
			if (Interrupter.get().hasCurrentThreadBeenInterruptedWithReason(task)) {
				throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed since the timeout was hit.");
			}
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
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

}
