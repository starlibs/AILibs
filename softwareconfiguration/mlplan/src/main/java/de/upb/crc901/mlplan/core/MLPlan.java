package de.upb.crc901.mlplan.core;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.core.events.ClassifierFoundEvent;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.core.HASCOFactory;
import hasco.core.HASCOSolutionCandidate;
import hasco.events.HASCOSolutionEvent;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MathExt;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassMeasureBuilder;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.SimpleEvaluatorMeasureBridge;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchInput;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlan extends AAlgorithm<Instances, Classifier> implements ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlan.class);
	private String loggerName;

	private Classifier selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private final TwoPhaseHASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String> twoPhaseHASCOFactory;
	private final OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private Instances dataShownToSearch = null;

	public MLPlan(final MLPlanBuilder builder, final Instances data) throws IOException {
		super(builder.getAlgorithmConfig(), data);
		builder.prepareNodeEvaluatorInFactoryWithData(data);

		/* sanity checks */
		this.logger.info("Starting an ML-Plan instance.");
		if (builder.getSearchSpaceConfigFile() == null || !builder.getSearchSpaceConfigFile().exists()) {
			throw new IllegalArgumentException("The search space configuration file must be set in MLPlanBuilder, and it must be set to a file that exists!");
		}
		if (builder.getClassifierFactory() == null) {
			throw new IllegalArgumentException("ClassifierFactory must be set in MLPlanBuilder!");
		}

		/* set evaluation measure bridge */
		ADecomposableDoubleMeasure<Double> measure = new MultiClassMeasureBuilder().getEvaluator(builder.getPerformanceMeasure());
		AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;
		if (builder.getUseCache()) {
			evaluationMeasurementBridge = new CacheEvaluatorMeasureBridge(measure, builder.getDBAdapter());
		} else {
			evaluationMeasurementBridge = new SimpleEvaluatorMeasureBridge(measure);
		}

		/* set up exact splits */
		double selectionDataPortion = this.getConfig().dataPortionForSelection();
		if (selectionDataPortion > 0) {
			List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(this.getInput(), this.getConfig().randomSeed(), selectionDataPortion);
			this.dataShownToSearch = selectionSplit.get(1);
		} else {
			this.dataShownToSearch = this.getInput();
		}
		if (this.dataShownToSearch.isEmpty()) {
			throw new IllegalStateException("Cannot search on no data.");
		}

		/* dynamically compute blow-ups */
		double blowUpInSelectionPhase = MathExt.round(1f / this.getConfig().getMCCVTrainFoldSizeDuringSearch() * this.getConfig().numberOfMCIterationsDuringSelection() / this.getConfig().numberOfMCIterationsDuringSearch(), 2);
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getConfig().dataPortionForSelection())) / this.getConfig().numberOfMCIterationsDuringSelection(), 2);
		this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

		/* create 2-phase software configuration problem */
		IObjectEvaluator<ComponentInstance, Double> searchBenchmark = new SearchPhasePipelineEvaluator(builder.getClassifierFactory(), evaluationMeasurementBridge, this.getConfig().numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
				this.getConfig().getMCCVTrainFoldSizeDuringSearch(), this.getConfig().randomSeed(), this.getConfig().timeoutForCandidateEvaluation());
		IObjectEvaluator<ComponentInstance, Double> selectionBenchmark = new SelectionPhasePipelineEvaluator(builder.getClassifierFactory(), evaluationMeasurementBridge, this.getConfig().numberOfMCIterationsDuringSelection(),
				MLPlan.this.getInput(), this.getConfig().getMCCVTrainFoldSizeDuringSelection(), this.getConfig().randomSeed(), this.getConfig().timeoutForCandidateEvaluation());
		TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(builder.getSearchSpaceConfigFile(), "AbstractClassifier", searchBenchmark, selectionBenchmark);

		/* create 2-phase HASCO */
		this.logger.info("Creating the twoPhaseHASCOFactory.");
		OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(builder.getClassifierFactory(), problem);
		HASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String, Double> hascoFactory = builder.getHASCOFactory();
		this.twoPhaseHASCOFactory = new TwoPhaseHASCOFactory<>(hascoFactory);
		this.twoPhaseHASCOFactory.setConfig(this.getConfig());
		this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.twoPhaseHASCOFactory);
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
						MLPlan.this.logger.info("Received new solution {} with score {} and evaluation time {}ms", builder.getClassifierFactory().getComponentInstantiation(solution.getComponentInstance()), solution.getScore(),
								solution.getTimeToEvaluateCandidate());
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						MLPlan.this.post(new ClassifierFoundEvent(MLPlan.this.getId(), builder.getClassifierFactory().getComponentInstantiation(solution.getComponentInstance()), solution.getScore()));
					} catch (ComponentInstantiationFailedException e) {
						e.printStackTrace();
					}
				} else {
					MLPlan.this.post(event);
				}
			}
		});
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case created: {
			AlgorithmInitializedEvent event = this.activate();

			/* check number of CPUs assigned */
			if (this.getConfig().cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
			}

			/* set timeout of optimizing factory */
			// this.twoPhaseHASCOFactory.setTimeout(this.getTimeout());

			/* communicate the parameters with which ML-Plan will run */
			if (this.logger.isInfoEnabled()) {
				this.logger.info(
						"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tMCCV for search: {} iterations with {}% for training\n\tMCCV for select: {} iterations with {}% for training\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
						this.getInput().relationName(), MultiClassPerformanceMeasure.ERRORRATE, this.getConfig().cpus(), this.getTimeout().seconds(), this.getConfig().timeoutForCandidateEvaluation() / 1000,
						this.getConfig().timeoutForNodeEvaluation() / 1000, this.getConfig().numberOfRandomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2),
						this.getConfig().numberOfMCIterationsDuringSearch(), (int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSearch()), this.getConfig().numberOfMCIterationsDuringSelection(),
						(int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSelection()), this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());
			}
			this.logger.info("Initializing the optimization factory.");
			this.optimizingFactory.init();
			this.logger.info("Started and activated ML-Plan.");
			return event;

		}
		case active: {

			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.selectedClassifier = this.optimizingFactory.call();
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			try {
				this.selectedClassifier.buildClassifier(this.getInput());
			} catch (Exception e) {
				throw new AlgorithmException(e, "Training the classifier failed!");
			}
			long endBuildTime = System.currentTimeMillis();
			this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			return this.terminate();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}

	}

	@Override
	public Classifier call() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
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
		this.logger.info("Setting logger of {} to {}.optimizingfactory", this.optimizingFactory.getClass().getName(), this.loggerName);
		this.optimizingFactory.setLoggerName(this.loggerName + ".optimizingfactory");

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

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	@SuppressWarnings("unchecked")
	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		return ((TwoPhaseHASCO<? extends GraphSearchInput<TFDNode, String>, TFDNode, String>) this.optimizingFactory.getOptimizer()).getGraphGenerator();
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	@Override
	public void cancel() {
		this.logger.info("Received cancel. First canceling optimizer, then invoking general shutdown.");
		this.optimizingFactory.cancel();
		this.logger.debug("Now canceling main ML-Plan routine");
		super.cancel();
		assert this.isCanceled() : "Canceled-flag is not positive at the end of the cancel routine!";
		this.logger.info("Completed cancellation of ML-Plan. Cancel status is {}", this.isCanceled());
	}
}
