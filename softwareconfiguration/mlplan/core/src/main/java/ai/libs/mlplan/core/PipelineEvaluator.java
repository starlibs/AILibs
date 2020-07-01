package ai.libs.mlplan.core;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.attributedobjects.IInformedObjectEvaluatorExtension;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IEvent;
import org.api4.java.common.event.IEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.ComponentInstanceUtil;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.timing.TimedObjectEvaluator;
import ai.libs.mlplan.core.events.SupervisedLearnerCreatedEvent;
import ai.libs.mlplan.core.events.TimeTrackingLearnerEvaluationEvent;
import ai.libs.mlplan.safeguard.AlwaysEvaluateSafeGuard;
import ai.libs.mlplan.safeguard.EvaluationSafeGuardException;
import ai.libs.mlplan.safeguard.EvaluationSafeGuardFiredEvent;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuard;

/**
 * Evaluator used in the search phase of mlplan.
 *
 * @author fmohr
 */
public class PipelineEvaluator extends TimedObjectEvaluator<ComponentInstance, Double> implements IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private static final String DEFAULT_PIPELINE_EVALUATOR_ID = "PipelineEvaluator";

	private String pipelineEvaluatorID = DEFAULT_PIPELINE_EVALUATOR_ID;
	private Logger logger = LoggerFactory.getLogger(PipelineEvaluator.class);

	private final EventBus eventBus = new EventBus();
	private final ILearnerFactory<? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> learnerFactory;
	private final ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> benchmark;
	private final Timeout timeoutForEvaluation;
	private Double bestScore = 1.0;

	private IEvaluationSafeGuard safeGuard;

	public PipelineEvaluator(final ILearnerFactory<? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> learnerFactory,
			final ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> benchmark, final Timeout timeoutForEvaluation) {
		this(learnerFactory, benchmark, timeoutForEvaluation, new AlwaysEvaluateSafeGuard());
	}

	public PipelineEvaluator(final ILearnerFactory<? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> learnerFactory,
			final ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> benchmark, final Timeout timeoutForEvaluation, final IEvaluationSafeGuard safeGuard) {
		super();
		this.learnerFactory = learnerFactory;
		this.benchmark = benchmark;
		if (benchmark instanceof IEventEmitter) {
			((IEventEmitter) benchmark).registerListener(this);
		}
		this.timeoutForEvaluation = timeoutForEvaluation;
		this.safeGuard = safeGuard;
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
		this.logger.debug("Query evaluation safe guard whether to evaluate this component instance for the given timeout {}.", this.timeoutForEvaluation);
		try {
			if (!this.safeGuard.predictWillAdhereToTimeout(c, this.timeoutForEvaluation)) {
				this.eventBus.post(new EvaluationSafeGuardFiredEvent(c));
				throw new EvaluationSafeGuardException("Evaluation safe guard prevents evaluation of component instance.", c);
			}
		} catch (EvaluationSafeGuardException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error("Could not use evaluation safe guard for component instance of {}. Continue with business as usual. Here is the stacktrace:", ComponentInstanceUtil.toComponentNameString(c), e);
		}

		try {
			if (this.benchmark instanceof IInformedObjectEvaluatorExtension) {
				((IInformedObjectEvaluatorExtension<Double>) this.benchmark).informAboutBestScore(this.bestScore);
			}

			this.logger.debug("Instantiate learner from component instance.");
			ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner = this.learnerFactory.getComponentInstantiation(c);
			this.eventBus.post(new SupervisedLearnerCreatedEvent(c, learner)); // inform listeners about the creation of the classifier

			ITimeTrackingLearner trackableLearner = new TimeTrackingLearnerWrapper(c, learner);
			trackableLearner.setPredictedInductionTime(c.getAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INDUCTION_TIME));
			trackableLearner.setPredictedInferenceTime(c.getAnnotation(IEvaluationSafeGuard.ANNOTATION_PREDICTED_INFERENCE_TIME));

			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Starting benchmark {} for classifier {}", this.benchmark, (learner instanceof ScikitLearnWrapper) ? learner.toString() : learner.getClass().getName());
			}

			Double score = this.benchmark.evaluate(trackableLearner);
			trackableLearner.setScore(score);
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Obtained score {} for classifier {}", score, (learner instanceof ScikitLearnWrapper) ? learner.toString() : learner.getClass().getName());
			}

			this.eventBus.post(new TimeTrackingLearnerEvaluationEvent(trackableLearner));

			this.safeGuard.updateWithActualInformation(c, trackableLearner);
			return score;
		} catch (ComponentInstantiationFailedException e) {
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e);
		}
	}

	@Override
	public void informAboutBestScore(final Double bestScore) {
		this.bestScore = bestScore;
	}

	@Override
	public Timeout getTimeout(final ComponentInstance item) {
		return this.timeoutForEvaluation;
	}

	@Override
	public String getMessage(final ComponentInstance item) {
		return "Pipeline evaluation phase";
	}

	public ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<?>> getBenchmark() {
		return this.benchmark;
	}

	public void setSafeGuard(final IEvaluationSafeGuard safeGuard) {
		if (safeGuard != null) {
			this.safeGuard = safeGuard;
		} else {
			this.safeGuard = new AlwaysEvaluateSafeGuard();
		}
	}

	public void setPipelineEvaluatorID(final String pipelineEvaluatorID) {
		this.pipelineEvaluatorID = pipelineEvaluatorID;
	}

	public String getPipelineEvaluatorID() {
		return this.pipelineEvaluatorID;
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
