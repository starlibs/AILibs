package ai.libs.mlplan.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.IInformedObjectEvaluatorExtension;
import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.basic.events.IEvent;
import ai.libs.jaicore.basic.events.IEventEmitter;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.timing.TimedObjectEvaluator;
import ai.libs.mlplan.core.events.ClassifierCreatedEvent;
import ai.libs.mlplan.multiclass.wekamlplan.IClassifierFactory;
import weka.classifiers.Classifier;

/**
 * Evaluator used in the search phase of mlplan.
 *
 * @author fmohr
 */
public class PipelineEvaluator extends TimedObjectEvaluator<ComponentInstance, Double> implements IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(PipelineEvaluator.class);

	private final EventBus eventBus = new EventBus();
	private final IClassifierFactory classifierFactory;
	private final IClassifierEvaluator benchmark;
	private final int timeoutForEvaluation;
	private Double bestScore = 1.0;

	public PipelineEvaluator(final IClassifierFactory classifierFactory, final IClassifierEvaluator benchmark, final int timeoutForEvaluation) {
		super();
		this.classifierFactory = classifierFactory;
		this.benchmark = benchmark;
		if (benchmark instanceof IEventEmitter) {
			((IEventEmitter) benchmark).registerListener(this);
		}
		this.timeoutForEvaluation = timeoutForEvaluation;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger name from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		if (this.benchmark instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger name of actual benchmark {} to {}.benchmark", this.benchmark.getClass().getName(), name);
			((ILoggingCustomizable) this.benchmark).setLoggerName(name + ".benchmark");
		} else {
			this.logger.info("Benchmark {} does not implement ILoggingCustomizable, not customizing its logger.", this.benchmark.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double evaluateSupervised(final ComponentInstance c) throws InterruptedException, ObjectEvaluationFailedException {
		this.logger.debug("Received request to evaluate component instance {}", c);
		try {
			if (this.benchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) this.benchmark).updateBestScore(this.bestScore);
			}
			Classifier classifier = this.classifierFactory.getComponentInstantiation(c);
			this.eventBus.post(new ClassifierCreatedEvent(c, classifier)); // inform listeners about the creation of the classifier
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Starting benchmark {} for classifier {}", this.benchmark, (classifier instanceof ScikitLearnWrapper) ? classifier.toString() : classifier.getClass().getName());
			}
			Double score = this.benchmark.evaluate(classifier);
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Obtained score {} for classifier {}", score, (classifier instanceof ScikitLearnWrapper) ? classifier.toString() : classifier.getClass().getName());
			}
			return score;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}
	}

	@Override
	public void updateBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

	@Override
	public long getTimeout(final ComponentInstance item) {
		return this.timeoutForEvaluation;
	}

	@Override
	public String getMessage(final ComponentInstance item) {
		return "Pipeline evaluation phase";
	}

	public IClassifierEvaluator getBenchmark() {
		return this.benchmark;
	}

	/**
	 * Here, we send a coupling event that informs the listener about which ComponentInstance has been used to create a classifier.
	 *
	 * @param listener
	 */
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}

	/**
	 * Forwards every incoming event e
	 *
	 * @param e
	 */
	@Subscribe
	public void receiveEvent(final IEvent e) {
		this.eventBus.post(e);
	}
}
