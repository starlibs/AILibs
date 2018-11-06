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
import de.upb.crc901.mlplan.multiclass.LossFunctionBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.SemanticNodeEvaluator;
import hasco.core.HASCOSolutionCandidate;
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
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.classifiers.Classifier;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class AutoFEMLComplete extends AbstractAutoFEMLClassifier
		implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, AutoFEWekaPipeline> {

	private Logger logger = LoggerFactory.getLogger(AutoFEMLComplete.class);
	private String loggerName;

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
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, Double> optimizingFactory;
	private final BasicMLEvaluator benchmark;
	private final AutoFEWekaPipelineFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;

	private double internalValidationErrorOfSelectedClassifier;
	private boolean enableVisualization;

	public AutoFEMLComplete(final long seed, final double subsampleRatio, final double mlplanSubsampleRatioFactor,
			final int minInstances, final MLPlanFEWekaClassifierConfig config, final AutoFEWekaPipelineFactory factory)
			throws IOException {

		// this.componentLoader = new ComponentLoader(new
		// File("model/MLPlanFEWeka.json"));
		this.componentFile = new File("model/MLPlanFEWeka.json");
		this.components = new ComponentLoader(this.componentFile).getComponents();

		this.rand = new Random(seed);

		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;

		this.config = config;
		this.benchmark = new LossFunctionBuilder().getEvaluator(new MLPlanWekaBuilder().getPerformanceMeasure());
		this.factory = factory;
		this.preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(this.getComponents(),
				FileUtil.readFileAsList(this.config.preferredComponents()));

	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {
		// this.setData(dataForComplete);
		this.setData(data);
		this.call();
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
	public void enableVisualization(final boolean enableVisualization) {
		this.enableVisualization = enableVisualization;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AutoFEWekaPipeline call() throws Exception {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.getSelectedPipeline();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public DataSet getInput() {
		return this.data;
	}

	@Override
	public void registerListener(Object listener) {
		this.optimizingFactory.registerListener(listener);

	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		double factor = 1;
		switch (timeUnit) {
		case MILLISECONDS:
			factor = 1 / 1000;
			break;
		case MINUTES:
			factor = 60;
			break;
		case HOURS:
			factor = 60 * 60;
			break;
		default:
			logger.warn("A timeout unit was used which is not supported. Timeout value '" + timeout
					+ "' is interpreted as seconds.");
		}
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_TIMEOUT, String.valueOf((int) (timeout * factor)));
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.state) {
		case created: {
			/* check whether data has been set */
			if (this.data == null) {
				throw new IllegalArgumentException("Data to work on is still NULL");
			}

			/* check number of CPUs assigned */
			if (this.config.cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
			}

			/* Subsample dataset to reduce computational effort. */
			logger.debug("Subsampling...");
			DataSet dataForComplete = DataSetUtils.subsample(data, this.subsampleRatio, this.minInstances, this.rand,
					this.mlplanSubsampleRatioFactor);
			dataForComplete.updateInstances();

			/* set up exact splits */
			float selectionDataPortion = this.config.dataPortionForSelection();
			if (selectionDataPortion > 0) {
				List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(dataForComplete.getInstances(),
						new Random(this.config.randomSeed()), selectionDataPortion);
				this.dataShownToSearch = selectionSplit.get(1);
			} else {
				this.dataShownToSearch = dataForComplete.getInstances();
			}
			if (this.dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}

			/* dynamically compute blow-ups */
			double blowUpInSelectionPhase = MathExt.round(1f / this.config.getMCCVTrainFoldSizeDuringSearch()
					* this.config.numberOfMCIterationsDuringSelection()
					/ this.config.numberOfMCIterationsDuringSearch(), 2);
			double blowUpInPostprocessing = MathExt.round((1 / (1 - this.config.dataPortionForSelection()))
					/ this.config.numberOfMCIterationsDuringSelection(), 2);
			this.config.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
			this.config.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS,
					String.valueOf(blowUpInPostprocessing));

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
			this.logger.info("Starting AutoFEMLComplete search.");
			this.logger.info("Using the following preferred node evaluator: {}", this.preferredNodeEvaluator);

			/* create HASCO problem */
			IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(
					this.benchmark, this.config.numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
					this.config.getMCCVTrainFoldSizeDuringSearch());
			IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark
					.evaluate(this.factory.getComponentInstantiation(c));
			IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

				@Override
				public Double evaluate(final Classifier object) throws Exception {

					/* first conduct MCCV */
					MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(
							AutoFEMLComplete.this.benchmark,
							AutoFEMLComplete.this.config.numberOfMCIterationsDuringSelection(),
							dataForComplete.getInstances(),
							AutoFEMLComplete.this.config.getMCCVTrainFoldSizeDuringSelection());
					mccv.evaluate(object);

					/* now retrieve .75-percentile from stats */
					double mean = mccv.getStats().getMean();
					double percentile = mccv.getStats().getPercentile(75f);
					AutoFEMLComplete.this.logger.info(
							"Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}",
							percentile, mean, mccv.getStats().getN());
					return percentile;
				}
			};
			IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> selectionBenchmark
					.evaluate(this.factory.getComponentInstantiation(c));
			TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(this.componentFile,
					"AutoFEMLPipeline", wrappedSearchBenchmark, wrappedSelectionBenchmark);

			/* configure and start optimizing factory */
			OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(
					this.factory, problem);
			this.hascoFactory = new TwoPhaseHASCOFactory();

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
			this.hascoFactory.setPreferredNodeEvaluator(
					new AutoFEMLPreferredNodeEvaluator(this.components, this.factory, this.config.maxPipelineSize()));
			this.hascoFactory.setConfig(this.config);
			this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.hascoFactory);
			this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
			this.optimizingFactory.setTimeout(this.config.timeout(), TimeUnit.SECONDS);
			this.optimizingFactory.registerListener(this);
			this.optimizingFactory.init();

			/* set state to active */
			this.state = AlgorithmState.active;
			return new AlgorithmInitializedEvent();
		}
		case active: {
			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.setSelectedPipeline(this.optimizingFactory.call());
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			this.selectedPipeline.buildClassifier(this.data);
			long endBuildTime = System.currentTimeMillis();
			this.logger.info(
					"Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms",
					endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			this.state = AlgorithmState.inactive;
			return new AlgorithmFinishedEvent();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.state);
		}
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched ML-Plan logger to {}", name);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
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

	public void setData(DataSet data) {
		this.data = data;
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data) {
		return new SemanticNodeEvaluator(getComponents(), data);
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeoutInS) {
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH,
				String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE,
				String.valueOf(timeoutInS * 1000));
	}

	@Subscribe
	@Override
	public void rcvHASCOSolutionEvent(SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> e) throws Exception {
		if (this.adapter != null) {
			AutoFEWekaPipeline pipe = factory
					.getComponentInstantiation(e.getSolutionCandidate().getComponentInstance());

			Map<String, Object> eval = new HashMap<>();
			eval.put("run_id", this.experimentID);
			eval.put("preprocessor", pipe.getFilterPipeline().toString());
			eval.put("classifier", pipe.getMLPipeline().toString());
			eval.put("errorRate", e.getSolutionCandidate().getScore());
			eval.put("time_train", e.getSolutionCandidate().getTimeToEvaluateCandidate());
			eval.put("time_predict", -1);
			try {
				this.adapter.insert(this.evalTable, eval);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return internalValidationErrorOfSelectedClassifier;
	}

}
