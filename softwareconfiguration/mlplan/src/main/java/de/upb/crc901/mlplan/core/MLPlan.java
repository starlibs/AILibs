package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import hasco.model.Component;
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
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassMeasureBuilder;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
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

	private final MLPlanBuilder builder;
	private final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;
	private Classifier selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private TwoPhaseHASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String> twoPhaseHASCOFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private Instances dataShownToSearch = null;

	public MLPlan(MLPlanBuilder builder, final Instances data) throws IOException {
		super(builder.getAlgorithmConfig(), data);
		this.builder = builder;
		builder.prepareNodeEvaluatorInFactoryWithData(data);

		/* sanity checks */
		logger.info("Starting an ML-Plan instance.");
		if (builder.getSearchSpaceConfigFile() == null || !builder.getSearchSpaceConfigFile().exists())
			throw new IllegalArgumentException("The search space configuration file must be set in MLPlanBuilder, and it must be set to a file that exists!");
		if (builder.getClassifierFactory() == null)
			throw new IllegalArgumentException("ClassifierFactory must be set in MLPlanBuilder!");

		/* set evaluation measure bridge */
		ADecomposableDoubleMeasure<Double> measure = new MultiClassMeasureBuilder().getEvaluator(builder.getPerformanceMeasure());
		if (builder.getUseCache()) {
			this.evaluationMeasurementBridge = new CacheEvaluatorMeasureBridge(measure, builder.getDBAdapter());
		} else {
			this.evaluationMeasurementBridge = new SimpleEvaluatorMeasureBridge(measure);
		}

		/* check whether data has been set */
		if (this.getInput() == null) {
			throw new IllegalArgumentException("Data to work on is still NULL");
		}

		/* check number of CPUs assigned */
		if (this.getConfig().cpus() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
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
		double blowUpInSelectionPhase = MathExt
				.round(1f / this.getConfig().getMCCVTrainFoldSizeDuringSearch() * this.getConfig().numberOfMCIterationsDuringSelection() / this.getConfig().numberOfMCIterationsDuringSearch(), 2);
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getConfig().dataPortionForSelection())) / this.getConfig().numberOfMCIterationsDuringSelection(), 2);
		this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

		/* communicate the parameters with which ML-Plan will run */
		this.logger.info(
				"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tMCCV for search: {} iterations with {}% for training\n\tMCCV for select: {} iterations with {}% for training\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
				this.getInput().relationName(), MultiClassPerformanceMeasure.ERRORRATE, this.getConfig().cpus(), this.getTimeout().seconds(), this.getConfig().timeoutForCandidateEvaluation() / 1000,
				this.getConfig().timeoutForNodeEvaluation() / 1000, this.getConfig().randomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2),
				this.getConfig().numberOfMCIterationsDuringSearch(), (int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSearch()), this.getConfig().numberOfMCIterationsDuringSelection(),
				(int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSelection()), this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());
		this.logger.info("Using the following HASCO factory: {}", builder.getHASCOFactory());

		/* create HASCO problem */
		IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.evaluationMeasurementBridge, this.getConfig().numberOfMCIterationsDuringSearch(),
				this.dataShownToSearch, this.getConfig().getMCCVTrainFoldSizeDuringSearch(), this.getConfig().randomSeed());

		IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> {
			try {
				if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
					CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) this.evaluationMeasurementBridge).getShallowCopy(c);

					long seed = this.getConfig().randomSeed() + c.hashCode();

					IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new MonteCarloCrossValidationEvaluator(bridge, this.getConfig().numberOfMCIterationsDuringSearch(),
							this.dataShownToSearch, this.getConfig().getMCCVTrainFoldSizeDuringSearch(), seed);

					return copiedSearchBenchmark.evaluate(this.builder.getClassifierFactory().getComponentInstantiation(c));
				}
				return searchBenchmark.evaluate(this.builder.getClassifierFactory().getComponentInstantiation(c));
			} catch (ComponentInstantiationFailedException e) {
				throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
			}
		};

		IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> {
			/* first conduct MCCV */
			AbstractEvaluatorMeasureBridge<Double, Double> bridge = this.evaluationMeasurementBridge;
			if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
				bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);

			}

			MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(bridge, MLPlan.this.getConfig().numberOfMCIterationsDuringSelection(), MLPlan.this.getInput(),
					MLPlan.this.getConfig().getMCCVTrainFoldSizeDuringSelection(), this.getConfig().randomSeed());
			try {
				mccv.evaluate(this.builder.getClassifierFactory().getComponentInstantiation(c));
			} catch (ComponentInstantiationFailedException e) {
				throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
			}

			/* now retrieve .75-percentile from stats */
			double mean = mccv.getStats().getMean();
			double percentile = mccv.getStats().getPercentile(75f);
			MLPlan.this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, mccv.getStats().getN());
			return percentile;
		};
		TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(this.builder.getSearchSpaceConfigFile(), "AbstractClassifier", wrappedSearchBenchmark,
				wrappedSelectionBenchmark);

		/* configure and start optimizing factory */
		logger.info("Creating the twoPhaseHASCOFactory.");
		OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.builder.getClassifierFactory(), problem);
		HASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String, Double> hascoFactory = builder.getHASCOFactory();
		this.twoPhaseHASCOFactory = new TwoPhaseHASCOFactory<>(hascoFactory);
		this.twoPhaseHASCOFactory.setConfig(this.getConfig());
		this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.twoPhaseHASCOFactory);
		this.logger.info("Setting logger directive of {} to {}", this.optimizingFactory, this.loggerName + ".2phasehasco");
		this.optimizingFactory.registerListener(new Object() {
			
			@Subscribe
			public void receiveEventFromFactory(AlgorithmEvent event) {
				if (event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent)
					return;
				if (event instanceof HASCOSolutionEvent) {
					@SuppressWarnings("unchecked")
					HASCOSolutionCandidate<Double> solution = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate();
					try {
						logger.info("Received new solution {} with score {} and evaluation time {}ms", builder.getClassifierFactory().getComponentInstantiation(solution.getComponentInstance()),
								solution.getScore(), solution.getTimeToEvaluateCandidate());
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						post(new ClassifierFoundEvent(getId(), builder.getClassifierFactory().getComponentInstantiation(solution.getComponentInstance()), solution.getScore()));
					} catch (ComponentInstantiationFailedException e) {
						e.printStackTrace();
					}
				}
				else
					post(event);
			}
		});
		this.optimizingFactory.setTimeout(this.getTimeout());
		logger.info("Initializing the optimization factory.");
		this.optimizingFactory.init();
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		switch (this.getState()) {
		case created: {
			AlgorithmInitializedEvent event = this.activate();
			logger.info("Started and activated ML-Plan.");
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
			this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime,
					endBuildTime - startOptimizationTime);
			return this.terminate();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}

	}

	@Override
	public Classifier call() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
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

	public File getComponentFile() {
		return this.builder.getSearchSpaceConfigFile();
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeout) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeout) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout * 1000));
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(builder.getComponents());
	}

	public void setRandomSeed(final int seed) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	@SuppressWarnings("unchecked")
	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		return ((TwoPhaseHASCO<? extends GraphSearchInput<TFDNode, String>, TFDNode, String>) optimizingFactory.getOptimizer()).getGraphGenerator();
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}
}
