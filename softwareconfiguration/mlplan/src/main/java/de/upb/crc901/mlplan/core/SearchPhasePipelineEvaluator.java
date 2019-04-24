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
		this.searchBenchmark = new MonteCarloCrossValidationEvaluator(this.config.getEvaluationMeasurementBridge(), this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.config.getData(), this.config.getTrainFoldSize(),
				this.config.getSeed());
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger name from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		if (this.searchBenchmark instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger name of actual benchmark {} to {}.benchmark", this.searchBenchmark.getClass().getName(), name);
			((ILoggingCustomizable) this.searchBenchmark).setLoggerName(name + ".benchmark");
		} else {
			this.logger.info("Benchmark {} does not implement ILoggingCustomizable, not customizing its logger.", this.searchBenchmark.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double evaluate(final ComponentInstance c) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
		TimeoutSubmitter sub = GlobalTimer.getInstance().getSubmitter();
		TimerTask task = sub.interruptMeAfterMS(this.config.getTimeoutForSolutionEvaluation(), "Timeout for pipeline in search phase.");
		try {
			if (this.config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
				CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) this.config.getEvaluationMeasurementBridge()).getShallowCopy(c);
				int subSeed = this.config.getSeed() + c.hashCode();
				IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.bestScore,
						this.config.getData(), this.config.getTrainFoldSize(), subSeed);
				return copiedSearchBenchmark.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c));
			}
			if (this.searchBenchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) this.searchBenchmark).updateBestScore(this.bestScore);
			}
			return this.searchBenchmark.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c));
		} catch (InterruptedException e) {
			this.logger.info("Received InterruptedException!");
			assert !Thread.currentThread().isInterrupted() : "The interrupt-flag should not be true when an InterruptedException is thrown! Stack trace of the InterruptedException is \n\t"
					+ Arrays.asList(e.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n\t"));
			this.logger.info("Checking whether interrupt is triggered by task {}", task);
			if (Interrupter.get().hasCurrentThreadBeenInterruptedWithReason(task)) {
				Thread.interrupted(); // reset thread interruption flag, because the thread is not really interrupted but should only stop the evaluation
				this.logger.info("This is a controlled interrupt of ourselves for task {}. Resetted thread interruption flag. Interrupt flag is now {}", task, Thread.currentThread().isInterrupted());
				Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				assert !Interrupter.get().hasCurrentThreadOpenInterrupts() : "There are still open interrupts!";
				this.logger.info("Throwing ObjectEvaluationFailedException. Interrupt flag is {}", Thread.currentThread().isInterrupted());
				throw new ObjectEvaluationFailedException("Evaluation of composition failed since the timeout was hit.");
			}
			this.logger.info("Recognized uncontrolled interrupt. Forwarding this exception.");
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		} finally {
			task.cancel();
			this.logger.debug("Canceled timeout job {}", task);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

}
