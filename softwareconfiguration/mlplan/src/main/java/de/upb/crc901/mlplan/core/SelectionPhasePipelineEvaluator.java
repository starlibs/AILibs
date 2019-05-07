package de.upb.crc901.mlplan.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
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
		this.logger.debug("Running evaluator {}", this.config.getClassifierEvaluator());
		try {
			return this.config.getClassifierEvaluator().evaluate(this.config.getClassifierFactory().getComponentInstantiation(c));
		} catch (InterruptedException e) {
			throw e;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		if (bestScore == null) {
			throw new IllegalArgumentException("Best known score must not be updated with NULL");
		}

	}

	@Override
	public long getTimeout(final ComponentInstance item) {
		return this.config.getTimeoutForSolutionEvaluation();
	}

	@Override
	public String getMessage(final ComponentInstance item) {
		return "Pipeline evaluation during selection phase for candidate " + item;
	}

}
