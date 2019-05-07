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
		this.searchBenchmark = config.getClassifierEvaluator();
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
	public Double evaluateSupervised(final ComponentInstance c) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			if (this.config.getEvaluationMeasurementBridge() instanceof CacheEvaluatorMeasureBridge) {
				CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) this.config.getEvaluationMeasurementBridge()).getShallowCopy(c);
				int subSeed = this.config.getSeed() + c.hashCode();
				IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new ProbabilisticMonteCarloCrossValidationEvaluator(bridge, this.config.getDatasetSplitter(), this.config.getNumMCIterations(), this.bestScore, this.config.getData(),
						this.config.getTrainFoldSize(), subSeed);
				return copiedSearchBenchmark.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c));
			}
			if (this.searchBenchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) this.searchBenchmark).updateBestScore(this.bestScore);
			}
			return this.searchBenchmark.evaluate(this.config.getClassifierFactory().getComponentInstantiation(c));
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

	public PipelineEvaluatorBuilder getConfig() {
		return this.config;
	}

	@Override
	public long getTimeout(final ComponentInstance item) {
		return this.config.getTimeoutForSolutionEvaluation();
	}

	@Override
	public String getMessage(final ComponentInstance item) {
		return "Pipeline evaluation during search phase";
	}
}
