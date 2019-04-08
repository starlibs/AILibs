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
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.concurrent.GlobalTimer;
import jaicore.concurrent.GlobalTimer.TimeoutSubmitter;
import jaicore.interrupt.Interrupter;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import weka.core.Instances;

/**
 * Evaluator used in the selection phase of mlplan. Uses MCCV by default, but can be configured to use other Benchmarks.
 * 
 * @author fmohr
 * @author jnowack
 */
public class SelectionPhasePipelineEvaluator implements IObjectEvaluator<ComponentInstance, Double>, IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SelectionPhasePipelineEvaluator.class);

	private final ClassifierFactory classifierFactory;
	private final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;

	private final int seed;
	private final int numMCIterations;
	private final Instances dataShownToSelectionPhase;
	private final double trainFoldSize;
	private final int timeoutForSolutionEvaluation;

	private Double bestScore;
	
	public SelectionPhasePipelineEvaluator(ClassifierFactory classifierFactory, AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge, int numMCIterations, Instances dataShownToSearch, double trainFoldSize, int seed,
			int timeoutForSolutionEvaluation) {
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

		ProbabilisticMonteCarloCrossValidationEvaluator mccv = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, numMCIterations, bestScore, dataShownToSelectionPhase, trainFoldSize, seed);

		DescriptiveStatistics stats = new DescriptiveStatistics();
		TimeoutSubmitter sub = GlobalTimer.getInstance().getSubmitter();
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
	
	@Override
	public void updateBestScore(Double bestScore) {
		this.bestScore = bestScore;
	}

}
