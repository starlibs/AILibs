package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import hasco.core.HASCOSolutionCandidate;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.FileUtil;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MathExt;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.classifiers.Classifier;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class AutoFEMLComplete extends AbstractAutoFEMLClassifier implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, AutoFEWekaPipeline> {

	private Logger logger = LoggerFactory.getLogger(AutoFEMLComplete.class);
	private String loggerName;

	private static final int NUMBER_OF_MC_ITERATIONS_IN_SEARCH = 3;
	private static final int NUMBER_OF_MC_FOLDS_IN_SEARCH = 5;
	private static final int NUMBER_OF_MC_ITERATIONS_IN_selection = 3;
	private static final int NUMBER_OF_MC_FOLDS_IN_SELECTION = 3;

	private Random rand;

	/* Subsampling parameters */
	private final double subsampleRatio;
	private final double mlplanSubsampleRatioFactor;
	private int minInstances;

	private final File componentFile;
	private final Collection<Component> components;

	private DataSet data;
	private Instances dataShownToSearch = null;

	/* HASCO members */
	private AlgorithmState state = AlgorithmState.created;
	private MLPlanFEWekaClassifierConfig config;
	private TwoPhaseHASCOFactory hascoFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, HASCOSolutionCandidate<Double>, Double> optimizingFactory;
	private final BasicMLEvaluator benchmark;
	private final AutoFEWekaPipelineFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;

	private double internalValidationErrorOfSelectedClassifier;
	private final String id = getClass().getName() + "-" + System.currentTimeMillis();

	public AutoFEMLComplete(final long seed, final double subsampleRatio, final double mlplanSubsampleRatioFactor, final int minInstances, final MLPlanFEWekaClassifierConfig config, final AutoFEWekaPipelineFactory factory)
			throws IOException {

		// this.componentLoader = new ComponentLoader(new
		// File("model/MLPlanFEWeka.json"));
		componentFile = new File("model/MLPlanFEWeka.json");
		components = new ComponentLoader(componentFile).getComponents();

		rand = new Random(seed);

		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;

		this.config = config;
		benchmark = new MulticlassEvaluator(rand);
		this.factory = factory;
		preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(getComponents(), FileUtil.readFileAsList(this.config.preferredComponents()));

	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {
		// this.setData(dataForComplete);
		setData(data);
		call();
		// this.setSelectedPipeline(this.call());

		// LOGGER.info("Setup MLPlanWithFeatureEngineering...");
		// HASCOSupervisedML.REQUESTED_INTERFACE = "AutoFEMLPipeline";
		// MLPlanWithFeatureEngineering mlplan = new
		// MLPlanWithFeatureEngineering(this.componentLoader);
		// mlplan.setNumberOfCPUs(this.cpus);
		// mlplan.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		// mlplan.setTimeout((int) this.globalTimeOut.seconds());
		//
		// LOGGER.info("Setup AutoFEWekaPipelineFactory...");
		// AutoFEWekaPipelineFactory factory = new AutoFEWekaPipelineFactory(
		// new FilterPipelineFactory(data.getIntermediateInstances().get(0).shape()),
		// new WEKAPipelineFactory());
		// mlplan.setFactory(factory);
		//
		// LOGGER.debug("Create search/selection split...");
		// List<DataSet> searchSelectSplit =
		// DataSetUtils.getStratifiedSplit(dataForComplete, new Random(this.seed),
		// mlplan.getConfig().selectionDataPortion());
		//
		// /* Setup node evaluators */
		// LOGGER.info("Setup node and object evaluators...");
		// AutoFEMLPreferredNodeEvaluator nodeEvaluator = new
		// AutoFEMLPreferredNodeEvaluator(
		// this.componentLoader.getComponents(), factory, this.maxPipelineSize);
		// mlplan.setPreferredNodeEvaluator(nodeEvaluator);
		//
		// AutoFEMLMCCVBenchmark benchmark = new
		// AutoFEMLMCCVBenchmark(searchSelectSplit.get(1), this.seed,
		// mlplan.getConfig().searchMCIterations(),
		// mlplan.getConfig().searchDataPortion());
		// mlplan.setBenchmark(benchmark);
		// benchmark.setAdapter(this.getAdapter());
		// benchmark.setEvalTable(this.getEvalTable());
		// benchmark.setExperimentID(this.getExperimentID());
		//
		// AutoFEMLMCCVBenchmark selectionBenchmark = new
		// AutoFEMLMCCVBenchmark(dataForComplete, this.seed,
		// mlplan.getConfig().searchMCIterations(),
		// mlplan.getConfig().searchDataPortion());
		// mlplan.setSelectionPhaseEvaluator(selectionBenchmark);
		// mlplan.enableVisualization(this.enableVisualization);
		//
		// /* Run feature engineering phase */
		// LOGGER.info("Run ML-Plan including Feature Engineering...");
		// mlplan.gatherSolutions(this.globalTimeOut);
		//
		// HASCOClassificationMLSolution<AutoFEWekaPipeline> solution =
		// mlplan.getCurrentlyBestSolution();
		// LOGGER.info(
		// "Found solution " + solution.getSolution().toString() + " with internal
		// score: " + solution.getScore()
		// + " and it took " + solution.getTimeToComputeScore() + "ms to compute its
		// score.");
		// this.setSelectedPipeline(solution.getSolution());
		// this.getSelectedPipeline().buildClassifier(dataForComplete);

	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return nextWithException();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AutoFEWekaPipeline call() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		while (hasNext()) {
			nextWithException();
		}
		return getSelectedPipeline();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public DataSet getInput() {
		return data;
	}

	@Override
	public void registerListener(final Object listener) {
		optimizingFactory.registerListener(listener);

	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		config.setProperty(MLPlanFEWekaClassifierConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public int getNumCPUs() {
		return config.cpus();
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		switch (state) {
		case created: {
			/* check whether data has been set */
			if (data == null) {
				throw new IllegalArgumentException("Data to work on is still NULL");
			}

			/* check number of CPUs assigned */
			if (config.cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + config.cpus());
			}

			/* Subsample dataset to reduce computational effort. */
			logger.debug("Subsampling...");
			DataSet dataForComplete = DataSetUtils.subsample(data, subsampleRatio, minInstances, rand, mlplanSubsampleRatioFactor);
			dataForComplete.updateInstances();
			logger.debug("Finished subsampling.");

			/* set up exact splits */
			double selectionDataPortion = config.dataPortionForSelection();
			if (selectionDataPortion > 0) {
				List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(dataForComplete.getInstances(), config.randomSeed(), selectionDataPortion);
				dataShownToSearch = selectionSplit.get(1);
			} else {
				dataShownToSearch = dataForComplete.getInstances();
			}
			if (dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}

			/* dynamically compute blow-ups */
			double blowUpInSelectionPhase = MathExt.round(1f / NUMBER_OF_MC_FOLDS_IN_SEARCH * (NUMBER_OF_MC_FOLDS_IN_SELECTION / NUMBER_OF_MC_ITERATIONS_IN_SEARCH), 2);
			double blowUpInPostprocessing = MathExt.round((1 / (1 - config.dataPortionForSelection())) / NUMBER_OF_MC_FOLDS_IN_SELECTION, 2);
			config.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
			config.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

			/* communicate the parameters with which ML-Plan will run */
			// this.logger.info(
			// "Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget:
			// {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation:
			// {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node
			// evaluation: {}\n\tPortion of data for selection phase: {}%\n\tMCCV for
			// search: {} iterations with {}% for training\n\tMCCV for select: {} iterations
			// with {}% for training\n\tBlow-ups are {} for selection phase and {} for
			// post-processing phase.",
			// this.data.relationName(), MultiClassPerformanceMeasure.ERRORRATE,
			// this.config.cpus(),
			// this.config.timeout(), this.config.timeoutForCandidateEvaluation() / 1000,
			// this.config.timeoutForNodeEvaluation() / 1000,
			// this.config.randomCompletions(),
			// MathExt.round(this.config.dataPortionForSelection() * 100, 2),
			// this.config.numberOfMCIterationsDuringSearch(),
			// (int) (100 * this.config.getMCCVTrainFoldSizeDuringSearch()),
			// this.config.numberOfMCIterationsDuringSelection(),
			// (int) (100 * this.config.getMCCVTrainFoldSizeDuringSelection()),
			// this.config.expectedBlowupInSelection(),
			// this.config.expectedBlowupInPostprocessing());
			logger.info("Starting AutoFEMLComplete search.");
			logger.info("Using the following preferred node evaluator: {}", preferredNodeEvaluator);

			/* create HASCO problem */
			IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(benchmark, NUMBER_OF_MC_ITERATIONS_IN_SEARCH, dataShownToSearch, NUMBER_OF_MC_FOLDS_IN_SEARCH);
			IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> {
				try {
					return searchBenchmark.evaluate(factory.getComponentInstantiation(c));
				} catch (ComponentInstantiationFailedException e1) {
					throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e1);
				}
			};
			IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

				@Override
				public Double evaluate(final Classifier object) throws ObjectEvaluationFailedException {

					logger.info("Evaluating object " + object.toString() + "...");

					/* first conduct MCCV */
					MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(benchmark, NUMBER_OF_MC_ITERATIONS_IN_selection, dataForComplete.getInstances(), NUMBER_OF_MC_FOLDS_IN_SELECTION);
					double score;
					try {
						score = mccv.evaluate(object);
					} catch (Exception e) {
						throw new ObjectEvaluationFailedException("Could not evaluate object", e);
					}
					return score;
				}
			};
			IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> {
				try {
					return selectionBenchmark.evaluate(factory.getComponentInstantiation(c));
				} catch (ComponentInstantiationFailedException e) {
					throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e);
				}
			};
			TwoPhaseSoftwareConfigurationProblem problem;
			try {
				problem = new TwoPhaseSoftwareConfigurationProblem(componentFile, "AutoFEMLPipeline", wrappedSearchBenchmark, wrappedSelectionBenchmark);
			} catch (IOException e) {
				throw new AlgorithmException(e, "Could not construct the configuration problem.");
			}

			/* configure and start optimizing factory */
			OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(factory, problem);
			hascoFactory = new TwoPhaseHASCOFactory();

			// PreferenceBasedNodeEvaluator preferenceNodeEvaluator = new
			// PreferenceBasedNodeEvaluator(this.components,
			// FileUtil.readFileAsList(this.config.preferredComponents()));
			// this.hascoFactory.setPreferredNodeEvaluator(new
			// AlternativeNodeEvaluator<TFDNode, Double>(
			// this.getSemanticNodeEvaluator(this.dataShownToSearch),
			// this.preferredNodeEvaluator));
			// this.hascoFactory.setPreferredNodeEvaluator(preferenceNodeEvaluator);
			// this.hascoFactory.setPreferredNodeEvaluator(new
			// AlternativeNodeEvaluator<TFDNode, Double>(
			// this.getSemanticNodeEvaluator(this.dataShownToSearch),
			// preferenceNodeEvaluator));

			// this.hascoFactory.setPreferredNodeEvaluator(n -> {
			// if (n.getParent() == null)
			// return 0.0;
			// else
			// return null;
			// });
			//			hascoFactory.setPreferredNodeEvaluator(new AutoFEMLPreferredNodeEvaluator(components, factory, config.maxPipelineSize()));
			hascoFactory.setConfig(config);
			optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
			optimizingFactory.setLoggerName(loggerName + ".2phasehasco");
			optimizingFactory.setTimeout(config.timeout(), TimeUnit.SECONDS);
			optimizingFactory.registerListener(this);
			optimizingFactory.init();

			/* set state to active */
			state = AlgorithmState.active;
			return new AlgorithmInitializedEvent(getId());
		}
		case active: {
			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			setSelectedPipeline(optimizingFactory.call());
			internalValidationErrorOfSelectedClassifier = optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			try {
				selectedPipeline.buildClassifier(data);
			} catch (Exception e) {
				throw new AlgorithmException(e, "Coul not build the selected pipeline");
			}
			long endBuildTime = System.currentTimeMillis();
			logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			state = AlgorithmState.inactive;
			return new AlgorithmFinishedEvent(getId());
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + state);
		}
	}

	@Override
	public void setLoggerName(final String name) {
		loggerName = name;
		logger.info("Switching logger name to {}", name);
		logger = LoggerFactory.getLogger(name);
		logger.info("Switched ML-Plan logger to {}", name);
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataSet getData() {
		return data;
	}

	public void setData(final DataSet data) {
		this.data = data;
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(components);
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(final Instances data) {
		return new WekaPipelineValidityCheckingNodeEvaluator(getComponents(), data);
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeoutInS) {
		config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeoutInS * 1000));
	}

	@Subscribe
	@Override
	public void rcvHASCOSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> e) throws Exception {
		if (adapter != null) {
			AutoFEWekaPipeline pipe = factory.getComponentInstantiation(e.getSolutionCandidate().getComponentInstance());

			Map<String, Object> eval = new HashMap<>();
			eval.put("run_id", experimentID);
			eval.put("preprocessor", pipe.getFilterPipeline().toString());
			eval.put("classifier", pipe.getMLPipeline().toString());
			eval.put("errorRate", e.getSolutionCandidate().getScore());
			eval.put("time_train", e.getSolutionCandidate().getTimeToEvaluateCandidate());
			eval.put("time_predict", -1);
			try {
				adapter.insert(evalTable, eval);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return internalValidationErrorOfSelectedClassifier;
	}

	@Override
	public void setMaxNumThreads(final int maxNumberOfThreads) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(final long timeout, final TimeUnit timeUnit) {
		setTimeout(new TimeOut(timeout, timeUnit));
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		config.setProperty(IAlgorithmConfig.K_TIMEOUT, "" + timeout.milliseconds());
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(config.timeout(), TimeUnit.MILLISECONDS);
	}

	@Override
	public IAlgorithmConfig getConfig() {
		return config;
	}

	@Override
	public String getId() {
		return id;
	}

}
