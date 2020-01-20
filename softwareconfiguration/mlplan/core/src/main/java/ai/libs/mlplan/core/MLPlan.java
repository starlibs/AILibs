package ai.libs.mlplan.core;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.ml.core.IDataConfigurable;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.control.IRandomConfigurable;
import org.api4.java.common.event.IEvent;
import org.api4.java.common.reconstruction.IReconstructible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOFactory;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.events.TwoPhaseHASCOPhaseSwitchEvent;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.OptimizingFactory;
import ai.libs.hasco.optimizingfactory.OptimizingFactoryProblem;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.reconstruction.ReconstructionUtil;
import ai.libs.jaicore.ml.core.dataset.DatasetUtil;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearnerEvaluatorConstructionFailedException;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import ai.libs.mlplan.core.events.MLPlanPhaseSwitchedEvent;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

public class MLPlan<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> extends AAlgorithm<ILabeledDataset<?>, L> implements ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlan.class);
	private String loggerName;

	private L selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private ComponentInstance componentInstanceOfSelectedClassifier;

	private final IMLPlanBuilder<L, ?> builder;
	private TwoPhaseHASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> twoPhaseHASCOFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, L, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private boolean buildSelectedClasifierOnGivenData = true;
	private final long seed;

	private long timestampAlgorithmStart;
	private boolean maintainReconstructibility = true;

	protected MLPlan(final IMLPlanBuilder<L, ?> builder, final ILabeledDataset<?> data) { // ML-Plan has a package visible constructor, because it should only be constructed using a builder
		super(builder.getAlgorithmConfig(), data);

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
		this.setTimeout(new Timeout(builder.getAlgorithmConfig().timeout(), TimeUnit.MILLISECONDS));
		Objects.requireNonNull(this.getInput());
		if (this.getInput().isEmpty()) {
			throw new IllegalArgumentException("Cannot run ML-Plan on empty dataset.");
		}
		this.seed = this.builder.getAlgorithmConfig().seed();
		if (this.getInput() instanceof IReconstructible) {
			this.maintainReconstructibility = ReconstructionUtil.areInstructionsNonEmptyIfReconstructibilityClaimed(this.getInput());
			if (!this.maintainReconstructibility) {
				this.logger.warn("The dataset claims to be reconstructible, but it does not carry any instructions. ML-Plan will not add reconstruction instructions.");
			}
		}
		else {
			this.maintainReconstructibility = false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case CREATED:
			this.setTimeoutPrecautionOffset(Math.max(5000, this.getTimeoutPrecautionOffset())); // minimum 5 seconds precaution offset for timeouts
			this.logger.info("Starting an ML-Plan instance. Timeout precaution is {}ms", this.getTimeoutPrecautionOffset());
			this.timestampAlgorithmStart = System.currentTimeMillis();
			this.setDeadline(); // algorithm execution starts NOW, set deadline

			/* check number of CPUs assigned */
			if (this.getConfig().cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
			}

			/* set up exact splits */
			final double dataPortionUsedForSelection = this.getConfig().dataPortionForSelection();
			ILabeledDataset<?> dataShownToSearch;
			ILabeledDataset<?> dataShownToSelection;
			if (dataPortionUsedForSelection > 0) {
				try {
					int seed = this.getConfig().randomSeed();
					IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> splitter = this.builder.getSearchSelectionDatasetSplitter();
					if (splitter == null) {
						throw new IllegalArgumentException("The builder does not specify a dataset splitter for the separation between search and selection phase data.");
					}
					this.logger.debug("Splitting given {} data points into search data ({}%) and selection data ({}%) with splitter {}.", this.getInput().size(), MathExt.round((1 - dataPortionUsedForSelection) * 100, 2),
							MathExt.round(dataPortionUsedForSelection * 100, 2), splitter.getClass().getName());
					if (splitter instanceof ILoggingCustomizable) {
						((ILoggingCustomizable) splitter).setLoggerName(this.getLoggerName() + ".searchselectsplitter");
					}
					List<ILabeledDataset<?>> split = splitter.split(this.getInput(), new Random(seed), dataPortionUsedForSelection);
					final int expectedSearchSize = (int) Math.round(this.getInput().size() * (1 - dataPortionUsedForSelection)); // attention; this is a bit tricky (data portion for selection is in 0)
					final int expectedSelectionSize = this.getInput().size() - expectedSearchSize;
					if (Math.abs(expectedSearchSize - split.get(1).size()) > 1 || Math.abs(expectedSelectionSize - split.get(0).size()) > 1) {
						throw new IllegalStateException("Invalid split produced by " + splitter.getClass().getName() + "! Split sizes are " + split.get(1).size() + "/" + split.get(0).size() + " but expected sizes were " + expectedSearchSize
								+ "/" + expectedSelectionSize);
					}
					dataShownToSearch = split.get(1); // attention; this is a bit tricky (data portion for selection is in 0)
					dataShownToSelection = this.getInput();
					this.logger.debug("Search/Selection split completed. Using {} data points in search and {} in selection.", dataShownToSearch.size(), dataShownToSelection.size());
				} catch (SplitFailedException e) {
					throw new AlgorithmException("Error in ML-Plan execution.", e);
				}
			} else {
				dataShownToSearch = this.getInput();
				dataShownToSelection = null;
				this.logger.debug("Selection phase de-activated. Not splitting the data and giving everything to the search.");
			}
			if (dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}
			if (dataShownToSelection != null && dataShownToSelection.size() < dataShownToSearch.size()) {
				throw new IllegalStateException("The search data (" + dataShownToSearch.size() + " data points) are bigger than the selection data (" + dataShownToSelection.size() + " data points)!");
			}

			/* check that class proportions are maintained */
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Class distribution is {}. Original class distribution was {}", DatasetUtil.getLabelCounts(dataShownToSearch), DatasetUtil.getLabelCounts(this.getInput()));
			}

			/* check that reconstructibility is preserved */
			if (this.maintainReconstructibility && ((IReconstructible) dataShownToSearch).getConstructionPlan().getInstructions().isEmpty()) {
				throw new IllegalStateException("Reconstructibility instructions have been lost in search/selection-split!");
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
			ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactoryForSearch = this.builder.getLearnerEvaluationFactoryForSearchPhase();
			if (evaluatorFactoryForSearch instanceof IRandomConfigurable) {
				((IRandomConfigurable) evaluatorFactoryForSearch).setRandom(new Random(this.seed));
			}
			if (evaluatorFactoryForSearch instanceof IDataConfigurable) {
				((IDataConfigurable) evaluatorFactoryForSearch).setData(dataShownToSearch);
			}
			ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactoryForSelection = this.builder.getLearnerEvaluationFactoryForSelectionPhase();
			if (evaluatorFactoryForSelection instanceof IRandomConfigurable) {
				((IRandomConfigurable) evaluatorFactoryForSelection).setRandom(new Random(this.seed));
			}
			if (evaluatorFactoryForSelection instanceof IDataConfigurable && dataShownToSelection != null) {
				((IDataConfigurable) evaluatorFactoryForSelection).setData(dataShownToSelection);
			}
			PipelineEvaluator classifierEvaluatorForSearch;
			PipelineEvaluator classifierEvaluatorForSelection;
			try {
				classifierEvaluatorForSearch = new PipelineEvaluator(this.builder.getLearnerFactory(), evaluatorFactoryForSearch.getLearnerEvaluator(), this.getConfig().timeoutForCandidateEvaluation());
				classifierEvaluatorForSelection = dataShownToSelection != null ? new PipelineEvaluator(this.builder.getLearnerFactory(), evaluatorFactoryForSelection.getLearnerEvaluator(), this.getConfig().timeoutForCandidateEvaluation())
						: null;
			} catch (LearnerEvaluatorConstructionFailedException e2) {
				throw new AlgorithmException("Could not create the pipeline evaluator", e2);
			}
			classifierEvaluatorForSearch.registerListener(this); // events will be forwarded
			if (classifierEvaluatorForSelection != null) {
				classifierEvaluatorForSelection.registerListener(this); // events will be forwarded
			}

			/* communicate the parameters with which ML-Plan will run */
			if (this.logger.isInfoEnabled()) {
				this.logger.info(
						"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tData points used during search: {}\n\tData points used during selection: {}\n\tPipeline evaluation during search: {}\n\tPipeline evaluation during selection: {}\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
						this.getInput().getRelationName(), this.getConfig().cpus(), this.getTimeout().seconds(), this.getConfig().timeoutForCandidateEvaluation() / 1000, this.getConfig().timeoutForNodeEvaluation() / 1000,
						this.getConfig().numberOfRandomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2), dataShownToSearch.size(), dataShownToSelection != null ? dataShownToSelection.size() : 0,
								classifierEvaluatorForSearch.getBenchmark(), classifierEvaluatorForSelection != null ? classifierEvaluatorForSelection.getBenchmark() : null, this.getConfig().expectedBlowupInSelection(),
										this.getConfig().expectedBlowupInPostprocessing());
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
			this.twoPhaseHASCOFactory.setConfig(this.getConfig().copy(TwoPhaseHASCOConfig.class)); // instantiate 2-Phase-HASCO with a config COPY to not have config changes in 2-Phase-HASCO impacts on the MLPlan configuration
			this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.twoPhaseHASCOFactory);
			this.logger.info("Setting logger of {} to {}.optimizingfactory", this.optimizingFactory.getClass().getName(), this.loggerName);
			this.optimizingFactory.setLoggerName(this.loggerName + ".optimizingfactory");
			this.optimizingFactory.registerListener(new Object() {
				@Subscribe
				public void receiveEventFromFactory(final IEvent event) {
					if (event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent) {
						return;
					}
					if (event instanceof TwoPhaseHASCOPhaseSwitchEvent) {
						MLPlan.this.post(new MLPlanPhaseSwitchedEvent(MLPlan.this));
					}
					else if (event instanceof HASCOSolutionEvent) {
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
							MLPlan.this.post(new ClassifierFoundEvent(MLPlan.this, solution.getComponentInstance(), MLPlan.this.builder.getLearnerFactory().getComponentInstantiation(solution.getComponentInstance()), solution.getScore()));
						} catch (ComponentInstantiationFailedException e) {
							MLPlan.this.logger.error("An issue occurred while preparing the description for the post of a ClassifierFoundEvent", e);
						}
					} else {
						MLPlan.this.post(event);
					}
				}
			});

			this.optimizingFactory.setTimeout(this.getRemainingTimeToDeadline());
			this.logger.info("Initializing the optimization factory.");
			this.optimizingFactory.init();
			AlgorithmInitializedEvent event = this.activate();
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
				this.logger.info(
						"Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms ({}ms of that on preparation and {}ms on essential optimization). The chosen classifier is: {}",
						endBuildTime - startBuildTime, endBuildTime - this.timestampAlgorithmStart, startOptimizationTime - this.timestampAlgorithmStart, endBuildTime - startOptimizationTime, this.selectedClassifier);
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

	public void setPortionOfDataForPhase2(final double portion) {
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
	public IPathSearchInput<TFDNode, String> getSearchProblemInputGenerator() {
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

	public IAlgorithm<?, ?> getSearch() {
		HASCO<?, ?, ?, ?> hasco = ((TwoPhaseHASCO<?, ?, ?>) this.optimizingFactory.getOptimizer()).getHasco();
		return hasco.getSearch();
	}

	@Subscribe
	public void receiveEvent(final IEvent e) {
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
