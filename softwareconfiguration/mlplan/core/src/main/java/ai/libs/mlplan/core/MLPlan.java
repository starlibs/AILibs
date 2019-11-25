package ai.libs.mlplan.core;

import java.io.IOException;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.events.AlgorithmFinishedEvent;
import org.api4.java.algorithm.events.AlgorithmInitializedEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.HASCOFactory;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.OptimizingFactory;
import ai.libs.hasco.optimizingfactory.OptimizingFactoryProblem;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.MCCVSplitEvaluationEvent;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearnerEvaluatorConstructionFailedException;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolatedEvent;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import ai.libs.mlplan.core.events.SupervisedLearnerCreatedEvent;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

public class MLPlan<I extends ILabeledInstance, D extends ILabeledDataset<I>, L extends ISupervisedLearner<I, D>> extends AAlgorithm<D, L> implements ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlan.class);
	private String loggerName;

	private L selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private ComponentInstance componentInstanceOfSelectedClassifier;

	private final IMLPlanBuilder<I, D, L, ?> builder;
	private final D data;
	private TwoPhaseHASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> twoPhaseHASCOFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, L, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private boolean buildSelectedClasifierOnGivenData = true;

	public MLPlan(final IMLPlanBuilder<I, D, L, ?> builder, final D data) {
		super(builder.getAlgorithmConfig(), data);
		builder.prepareNodeEvaluatorInFactoryWithData(data);

		/* sanity checks */
		if (builder.getSearchSpaceConfigFile() == null || !builder.getSearchSpaceConfigFile().exists()) {
			throw new IllegalArgumentException("The search space configuration file must be set in MLPlanBuilder, and it must be set to a file that exists!");
		}
		if (builder.getLearnerFactory() == null) {
			throw new IllegalArgumentException("ClassifierFactory must be set in MLPlanBuilder!");
		}
		if (builder.getRequestedInterface() == null || builder.getRequestedInterface().isEmpty()) {
			throw new IllegalArgumentException("No requested HASCO interface defined!");
		}
		/* store builder and data for main algorithm */
		this.builder = builder;
		this.data = data;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case CREATED:
			this.logger.info("Starting an ML-Plan instance.");
			AlgorithmInitializedEvent event = this.activate();

			/* check number of CPUs assigned */
			if (this.getConfig().cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
			}

			/* set up exact splits */
			final double dataPortionUsedForSelection = this.getConfig().dataPortionForSelection();
			this.logger.debug("Splitting given {} data points into search data ({}%) and selection data ({}%).", this.data.size(), MathExt.round((1 - dataPortionUsedForSelection) * 100, 2), MathExt.round(dataPortionUsedForSelection, 2));
			D dataShownToSearch;
			if (dataPortionUsedForSelection > 0) {
				try {
					int seed = this.getConfig().randomSeed();
					IFoldSizeConfigurableRandomDatasetSplitter<D> splitter = this.builder.getSearchSelectionDatasetSplitter();
					if (splitter == null) {
						throw new IllegalArgumentException("The builder does not specify a dataset splitter");
					}
					dataShownToSearch = splitter.split(this.getInput(), new Random(seed), dataPortionUsedForSelection).get(1); // attention; this is a bit tricky (data portion for selection is in 0)
				} catch (SplitFailedException e) {
					throw new AlgorithmException("Error in ML-Plan execution.", e);
				}
			} else {
				dataShownToSearch = this.getInput();
			}
			if (dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}

			/* dynamically compute blow-ups */
			if (Double.isNaN(this.getConfig().expectedBlowupInSelection())) {
				double blowUpInSelectionPhase = 1;
				this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
				this.logger.info("No expected blow-up for selection phase has been defined. Automatically configuring {}", blowUpInSelectionPhase);
			}
			if (!this.buildSelectedClasifierOnGivenData) {
				this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(0));
				this.logger.info("Selected classifier won't be built, so now blow-up is calculated.");
			} else if (Double.isNaN(this.getConfig().expectedBlowupInPostprocessing())) {
				double blowUpInPostprocessing = 1;
				this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));
				this.logger.info("No expected blow-up for postprocessing phase has been defined. Automatically configuring {}", blowUpInPostprocessing);
			}

			/* setup the pipeline evaluators */
			this.logger.debug("Setting up the pipeline evaluators.");
			PipelineEvaluator<I, D> classifierEvaluatorForSearch;
			PipelineEvaluator<I, D> classifierEvaluatorForSelection;
			try {
				classifierEvaluatorForSearch = this.builder.getClassifierEvaluationInSearchPhase(dataShownToSearch, this.getConfig().randomSeed(), MLPlan.this.getInput().size());
				classifierEvaluatorForSelection = this.builder.getClassifierEvaluationInSelectionPhase(dataShownToSearch, this.getConfig().randomSeed());
			} catch (LearnerEvaluatorConstructionFailedException e2) {
				throw new AlgorithmException("Could not create the pipeline evaluator", e2);
			}
			classifierEvaluatorForSearch.registerListener(this); // events will be forwarded
			classifierEvaluatorForSelection.registerListener(this); // events will be forwarded

			/* communicate the parameters with which ML-Plan will run */
			if (this.logger.isInfoEnabled()) {
				this.logger.info(
						"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tPipeline evaluation during search: {}\n\tPipeline evaluation during selection: {}\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
						this.getInput().hashCode(), this.builder.getPerformanceMeasure(), this.getConfig().cpus(), this.getTimeout().seconds(), this.getConfig().timeoutForCandidateEvaluation() / 1000,
						this.getConfig().timeoutForNodeEvaluation() / 1000, this.getConfig().numberOfRandomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2), classifierEvaluatorForSearch.getBenchmark(),
						classifierEvaluatorForSelection.getBenchmark(), this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());
			}

			/* create 2-phase software configuration problem */
			this.logger.debug("Creating 2-phase software configuration problem.");
			TwoPhaseSoftwareConfigurationProblem problem = null;
			try {
				problem = new TwoPhaseSoftwareConfigurationProblem(this.builder.getSearchSpaceConfigFile(), this.builder.getRequestedInterface(), classifierEvaluatorForSearch, classifierEvaluatorForSelection);
			} catch (IOException e1) {
				throw new AlgorithmException("Could not activate ML-Plan!", e1);
			}

			/* create 2-phase HASCO */
			this.logger.info("Creating the twoPhaseHASCOFactory.");
			OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, L, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.builder.getLearnerFactory(), problem);
			HASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> hascoFactory = this.builder.getHASCOFactory();
			this.twoPhaseHASCOFactory = new TwoPhaseHASCOFactory<>(hascoFactory);

			this.twoPhaseHASCOFactory.setConfig(this.getConfig());
			this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.twoPhaseHASCOFactory);
			this.logger.info("Setting logger of {} to {}.optimizingfactory", this.optimizingFactory.getClass().getName(), this.loggerName);
			this.optimizingFactory.setLoggerName(this.loggerName + ".optimizingfactory");
			this.optimizingFactory.registerListener(new Object() {
				@Subscribe
				public void receiveEventFromFactory(final AlgorithmEvent event) {
					if (event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent) {
						return;
					}
					if (event instanceof HASCOSolutionEvent) {
						@SuppressWarnings("unchecked")
						HASCOSolutionCandidate<Double> solution = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate();
						try {
							MLPlan.this.logger.info("Received new solution {} with score {} and evaluation time {}ms", solution.getComponentInstance().getNestedComponentDescription(), solution.getScore(),
									solution.getTimeToEvaluateCandidate());
						} catch (Exception e) {
							MLPlan.this.logger.warn("Could not print log due to exception while preparing the log message.", e);
						}

						if (dataPortionUsedForSelection == 0.0 && solution.getScore() < MLPlan.this.internalValidationErrorOfSelectedClassifier) {
							try {
								MLPlan.this.selectedClassifier = MLPlan.this.builder.getLearnerFactory().getComponentInstantiation(solution.getComponentInstance());
								MLPlan.this.internalValidationErrorOfSelectedClassifier = solution.getScore();
								MLPlan.this.componentInstanceOfSelectedClassifier = solution.getComponentInstance();
							} catch (ComponentInstantiationFailedException e) {
								MLPlan.this.logger.error("Could not update selectedClassifier with newly best seen solution due to issues building the classifier from its ComponentInstance description.", e);
							}
						}

						try {
							MLPlan.this.post(
									new ClassifierFoundEvent(MLPlan.this.getId(), solution.getComponentInstance(), MLPlan.this.builder.getLearnerFactory().getComponentInstantiation(solution.getComponentInstance()), solution.getScore()));
						} catch (ComponentInstantiationFailedException e) {
							MLPlan.this.logger.error("An issue occurred while preparing the description for the post of a ClassifierFoundEvent", e);
						}
					} else {
						MLPlan.this.post(event);
					}
				}
			});

			this.logger.info("Initializing the optimization factory.");
			this.optimizingFactory.init();
			this.logger.info("Started and activated ML-Plan.");
			return event;

		case ACTIVE:

			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			try {
				this.selectedClassifier = this.optimizingFactory.call();
				this.logger.info("2-Phase-HASCO has chosen classifier {}, which will now be built on the entire data given, i.e. {} data points.", this.selectedClassifier, this.getInput().size());
			} catch (AlgorithmException | InterruptedException | AlgorithmExecutionCanceledException | AlgorithmTimeoutedException e) {
				this.terminate(); // send the termination event
				throw e;
			}
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			this.componentInstanceOfSelectedClassifier = this.optimizingFactory.getComponentInstanceOfObject();
			if (this.buildSelectedClasifierOnGivenData) {
				long startBuildTime = System.currentTimeMillis();
				try {
					this.selectedClassifier.fit(this.getInput());
				} catch (Exception e) {
					throw new AlgorithmException("Training the classifier failed!", e);
				}
				long endBuildTime = System.currentTimeMillis();
				this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms. The chosen classifier is: {}", endBuildTime - startBuildTime,
						endBuildTime - startOptimizationTime, this.selectedClassifier);
			} else {
				this.logger.info("Selected model has not been built, since model building has been disabled. Total construction time was {}ms.", System.currentTimeMillis() - startOptimizationTime);
			}
			return this.terminate();

		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}

	}

	@Override
	public L call() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.selectedClassifier;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated ML-Plan logger {}. Now setting logger of twoPhaseHASCO to {}.2phasehasco", name, name);
		if (this.optimizingFactory != null) {
			this.logger.info("Setting logger of {} to {}.optimizingfactory", this.optimizingFactory.getClass().getName(), this.loggerName);
			this.optimizingFactory.setLoggerName(this.loggerName + ".optimizingfactory");
		} else {
			this.logger.debug("Optimizingfactory has not been set yet, so not customizing its logger.");
		}

		this.logger.info("Switched ML-Plan logger to {}", name);
	}

	public void setPortionOfDataForPhase2(final float portion) {
		this.getConfig().setProperty(MLPlanClassifierConfig.SELECTION_PORTION, String.valueOf(portion));
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public MLPlanClassifierConfig getConfig() {
		return (MLPlanClassifierConfig) super.getConfig();
	}

	public void setRandomSeed(final int seed) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	public L getSelectedClassifier() {
		return this.selectedClassifier;
	}

	public ComponentInstance getComponentInstanceOfSelectedClassifier() {
		return this.componentInstanceOfSelectedClassifier;
	}

	@SuppressWarnings("unchecked")
	public IGraphSearchInput<TFDNode, String> getSearchProblemInputGenerator() {
		return ((TwoPhaseHASCO<? extends GraphSearchInput<TFDNode, String>, TFDNode, String>) this.optimizingFactory.getOptimizer()).getGraphSearchInput();
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	@Override
	public synchronized void cancel() {
		this.logger.info("Received cancel. First canceling optimizer, then invoking general shutdown.");
		this.optimizingFactory.cancel();
		this.logger.debug("Now canceling main ML-Plan routine");
		super.cancel();
		assert this.isCanceled() : "Canceled-flag is not positive at the end of the cancel routine!";
		this.logger.info("Completed cancellation of ML-Plan. Cancel status is {}", this.isCanceled());
	}

	public OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, L, HASCOSolutionCandidate<Double>, Double> getOptimizingFactory() {
		return this.optimizingFactory;
	}

	@Subscribe
	public void receiveClassifierCreatedEvent(final SupervisedLearnerCreatedEvent e) {
		this.post(e);
	}

	@Subscribe
	public void receiveClassifierCreatedEvent(final LearningCurveExtrapolatedEvent e) {
		this.post(e);
	}

	@Subscribe
	public void receiveClassifierCreatedEvent(final MCCVSplitEvaluationEvent e) {
		this.post(e);
	}

	public TwoPhaseHASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> getTwoPhaseHASCOFactory() {
		return this.twoPhaseHASCOFactory;
	}

	public boolean isBuildSelectedClasifierOnGivenData() {
		return this.buildSelectedClasifierOnGivenData;
	}

	public void setBuildSelectedClasifierOnGivenData(final boolean buildSelectedClasifierOnGivenData) {
		this.buildSelectedClasifierOnGivenData = buildSelectedClasifierOnGivenData;
	}
}