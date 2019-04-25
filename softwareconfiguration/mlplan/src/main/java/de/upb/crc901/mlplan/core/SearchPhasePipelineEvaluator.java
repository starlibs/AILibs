package de.upb.crc901.mlplan.core;

import java.util.Arrays;
import java.util.TimerTask;
import java.util.stream.Collectors;

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
import jaicore.concurrent.GlobalTimer;
import jaicore.concurrent.GlobalTimer.TimeoutSubmitter;
import jaicore.interrupt.Interrupter;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import weka.classifiers.Classifier;

/**
 * Evaluator used in the search phase of mlplan. Uses MCCV by default, but can be configured to use other Benchmarks.
 *
 * @author fmohr
 * @author jnowack
 */
public class SearchPhasePipelineEvaluator implements IObjectEvaluator<ComponentInstance, Double>, IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SearchPhasePipelineEvaluator.class);

	private final PipelineEvaluatorBuilder config;
	private IObjectEvaluator<Classifier, Double> searchBenchmark;
	private Double bestScore = 1.0;

	public SearchPhasePipelineEvaluator(final PipelineEvaluatorBuilder config) {
		super();
		this.config = config;
		searchBenchmark = new MonteCarloCrossValidationEvaluator(this.config.getEvaluationMeasurementBridge(), this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.config.getData(), this.config.getTrainFoldSize(),
				this.config.getSeed());
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		logger.info("Switching logger name from {} to {}", logger.getName(), name);
		logger = LoggerFactory.getLogger(name);
		if (searchBenchmark instanceof ILoggingCustomizable) {
			logger.info("Setting logger name of actual benchmark {} to {}.benchmark", searchBenchmark.getClass().getName(), name);
			((ILoggingCustomizable) searchBenchmark).setLoggerName(name + ".benchmark");
		} else {
			logger.info("Benchmark {} does not implement ILoggingCustomizable, not customizing its logger.", searchBenchmark.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double evaluate(final ComponentInstance c) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
		Thread currentThread = Thread.currentThread();
		TimeoutSubmitter sub = GlobalTimer.getInstance().getSubmitter();
		String reason = "Timeout for pipeline in search phase for component instance with hash code " + c.hashCode();
		TimerTask task = sub.interruptMeAfterMS(config.getTimeoutForSolutionEvaluation(), reason);
		try {
			if (config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
				CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) config.getEvaluationMeasurementBridge()).getShallowCopy(c);
				int subSeed = config.getSeed() + c.hashCode();
				IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, config.getDatasetSplitter(), config.getNumMCIterations(), bestScore,
						config.getData(), config.getTrainFoldSize(), subSeed);
				return copiedSearchBenchmark.evaluate(config.getClassifierFactory().getComponentInstantiation(c));
			}
			if (searchBenchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) searchBenchmark).updateBestScore(bestScore);
			}
			return searchBenchmark.evaluate(config.getClassifierFactory().getComponentInstantiation(c));
		} catch (InterruptedException e) {
			logger.info("Received InterruptedException!");
			assert !currentThread.isInterrupted() : "The interrupt-flag should not be true when an InterruptedException is thrown! Stack trace of the InterruptedException is \n\t"
			+ Arrays.asList(e.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n\t"));
			logger.info("Checking whether interrupt is triggered by task {}", task);
			Interrupter interrupter = Interrupter.get();
			synchronized (interrupter) {
				if (interrupter.hasCurrentThreadBeenInterruptedWithReason(task)) {
					Thread.interrupted(); // reset thread interruption flag, because the thread is not really interrupted but should only stop the evaluation
					logger.info("This is a controlled interrupt of ourselves for task {}. Resetted thread interruption flag. Interrupt flag is now {}", task, currentThread.isInterrupted());
					Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
					assert !Interrupter.get().hasCurrentThreadOpenInterrupts() : "There are still open interrupts!";
					logger.info("Throwing ObjectEvaluationFailedException. Interrupt flag is {}", currentThread.isInterrupted());
					throw new ObjectEvaluationFailedException("Evaluation of composition failed since the timeout was hit.");
				}
				logger.info("Recognized uncontrolled interrupt. Black-Listing the own interrupt-reason {} (because we will not handle it later on), canceling task, and forwarding the InterruptException.", task);
				interrupter.avoidInterrupt(currentThread, task);
			}
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		} finally {
			task.cancel();
			logger.debug("Canceled timeout job {}", task);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

	public PipelineEvaluatorBuilder getConfig() {
		return config;
	}
}
