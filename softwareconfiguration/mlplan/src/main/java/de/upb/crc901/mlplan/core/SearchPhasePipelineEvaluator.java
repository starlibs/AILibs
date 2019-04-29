package de.upb.crc901.mlplan.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.ProbabilisticMonteCarloCrossValidationEvaluator;
import jaicore.timing.TimedObjectEvaluator;
import weka.classifiers.Classifier;

/**
 * Evaluator used in the search phase of mlplan. Uses MCCV by default, but can be configured to use other Benchmarks.
 *
 * @author fmohr
 * @author jnowack
 */
public class SearchPhasePipelineEvaluator extends TimedObjectEvaluator<ComponentInstance, Double> implements IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

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
	public Double evaluateSupervised(final ComponentInstance c) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			if (config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
				CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) config.getEvaluationMeasurementBridge()).getShallowCopy(c);
				int subSeed = config.getSeed() + c.hashCode();
				IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, config.getDatasetSplitter(), config.getNumMCIterations(), bestScore, config.getData(),
						config.getTrainFoldSize(), subSeed);
				return copiedSearchBenchmark.evaluate(config.getClassifierFactory().getComponentInstantiation(c));
			}
			if (searchBenchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) searchBenchmark).updateBestScore(bestScore);
			}
			return searchBenchmark.evaluate(config.getClassifierFactory().getComponentInstantiation(c));
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

	public PipelineEvaluatorBuilder getConfig() {
		return config;
	}

	@Override
	public long getTimeout(ComponentInstance item) {
		return config.getTimeoutForSolutionEvaluation();
	}

	@Override
	public String getMessage(ComponentInstance item) {
		return "Pipeline evaluation during search phase";
	}
}
