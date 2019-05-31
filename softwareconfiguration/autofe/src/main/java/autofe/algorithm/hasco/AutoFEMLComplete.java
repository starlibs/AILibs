package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.OptimizingFactory;
import ai.libs.hasco.optimizingfactory.OptimizingFactoryProblem;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.EAlgorithmState;
import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.jaicore.basic.algorithm.IAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import ai.libs.jaicore.ml.core.exception.TrainingException;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.classifiers.Classifier;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class AutoFEMLComplete extends AbstractAutoFEMLClassifier implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, AutoFEWekaPipeline> {

	private Logger logger = LoggerFactory.getLogger(AutoFEMLComplete.class);
	private String loggerName;

	private static final int NUMBER_OF_MC_ITERATIONS_IN_SEARCH = 1;
	private static final int NUMBER_OF_MC_ITERATIONS_IN_SELECTION = 3;
	private static final int NUMBER_OF_MC_FOLDS_IN_SELECTION = 3;

	private static final String NOT_SUPPORTED_METHOD_MESSAGE = "Not supported yet.";

	private Random rand;

	/* Subsampling parameters */
	private final double subsampleRatio;
	private final double mlplanSubsampleRatioFactor;
	private int minInstances;

	private final File componentFile;
	private final Collection<Component> components;

	private DataSet data;

	/* HASCO members */
	private EAlgorithmState state = EAlgorithmState.CREATED;
	private MLPlanFEWekaClassifierConfig config;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, HASCOSolutionCandidate<Double>, Double> optimizingFactory;
	private final ISplitBasedClassifierEvaluator<Double> benchmark;
	private final AutoFEWekaPipelineFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;

	private double internalValidationErrorOfSelectedClassifier;
	private final String id = this.getClass().getName() + "-" + System.currentTimeMillis();

	public AutoFEMLComplete(final long seed, final double subsampleRatio, final double mlplanSubsampleRatioFactor,
			final int minInstances, final MLPlanFEWekaClassifierConfig config, final AutoFEWekaPipelineFactory factory)
					throws IOException {

		this.componentFile = new File("model/MLPlanFEWeka.json");
		this.components = new ComponentLoader(this.componentFile).getComponents();

		this.rand = new Random(seed);

		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;

		this.config = config;
		this.benchmark = new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss());
		this.factory = factory;
		this.preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(this.getComponents(), FileUtil.readFileAsList(this.config.preferredComponents()));

	}

	@Override
	public void buildClassifier(final DataSet data) throws TrainingException {
		this.setData(data);
		try {
			this.call();
		} catch (Exception e) {
			throw new TrainingException("Could not build classifier due to an exception.", e);
		}
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return new Iterator<AlgorithmEvent>() {
			@Override
			public boolean hasNext() {
				return AutoFEMLComplete.this.hasNext();
			}

			@Override
			public AlgorithmEvent next() {
				try {
					return AutoFEMLComplete.this.nextWithException();
				} catch (Exception e) {
					throw new NoSuchElementException("Can not return next element due to: " + e.getMessage());
				}
			}
		};
	}

	@Override
	public boolean hasNext() {
		return this.state != EAlgorithmState.INACTIVE;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			throw new NoSuchElementException("Can not return next element due to: " + e.getMessage());
		}
	}

	@Override
	public AutoFEWekaPipeline call() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.getSelectedPipeline();
	}

	@Override
	public void cancel() {
		throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
	}

	@Override
	public DataSet getInput() {
		return this.data;
	}

	@Override
	public void registerListener(final Object listener) {
		this.optimizingFactory.registerListener(listener);

	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public int getNumCPUs() {
		return this.config.cpus();
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		switch (this.state) {
		case CREATED:
			return this.setupSearch();
		case ACTIVE:
			return this.search();
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.state);
		}
	}

	private AlgorithmEvent setupSearch() throws AlgorithmException {
		/* check whether data has been set */
		if (this.data == null) {
			throw new IllegalArgumentException("Data to work on is still NULL");
		}

		/* check number of CPUs assigned */
		if (this.config.cpus() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
		}

		/* Subsample dataset to reduce computational effort. */
		this.logger.debug("Subsampling...");
		DataSet dataForComplete = DataSetUtils.subsample(this.data, this.subsampleRatio, this.minInstances, this.rand, this.mlplanSubsampleRatioFactor);
		dataForComplete.updateInstances();
		this.logger.debug("Finished subsampling.");

		/* set up exact splits */
		double selectionDataPortion = this.config.dataPortionForSelection();
		Instances dataShownToSearch;
		if (selectionDataPortion > 0) {
			List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(dataForComplete.getInstances(), this.config.randomSeed(), selectionDataPortion);
			dataShownToSearch = selectionSplit.get(1);
		} else {
			dataShownToSearch = dataForComplete.getInstances();
		}
		if (dataShownToSearch.isEmpty()) {
			throw new IllegalStateException("Cannot search on no data.");
		}

		/* dynamically compute blow-ups */
		this.logger.info("Starting AutoFEMLComplete search.");
		this.logger.info("Using the following preferred node evaluator: {}", this.preferredNodeEvaluator);

		/* create HASCO problem */
		IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.benchmark, NUMBER_OF_MC_ITERATIONS_IN_SEARCH, dataShownToSearch, 0.7, this.config.seed());
		IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> {
			try {
				return searchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
			} catch (ComponentInstantiationFailedException e1) {
				throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e1);
			}
		};
		IObjectEvaluator<Classifier, Double> selectionBenchmark = object -> {

			this.logger.info("Evaluating object {}...", object);

			/* first conduct MCCV */
			MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(this.benchmark, NUMBER_OF_MC_ITERATIONS_IN_SELECTION, dataForComplete.getInstances(), NUMBER_OF_MC_FOLDS_IN_SELECTION, this.config.seed());
			double score;
			try {
				score = mccv.evaluate(object);
			} catch (Exception e) {
				throw new ObjectEvaluationFailedException("Could not evaluate object", e);
			}
			return score;
		};
		IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> {
			try {
				return selectionBenchmark.evaluate(this.factory.getComponentInstantiation(c));
			} catch (ComponentInstantiationFailedException e) {
				throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e);
			}
		};
		TwoPhaseSoftwareConfigurationProblem problem;
		try {
			problem = new TwoPhaseSoftwareConfigurationProblem(this.componentFile, "AutoFEMLPipeline", wrappedSearchBenchmark, wrappedSelectionBenchmark);
		} catch (IOException e) {
			throw new AlgorithmException(e, "Could not construct the configuration problem.");
		}

		/* configure and start optimizing factory */
		OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.factory, problem);
		TwoPhaseHASCOFactory hascoFactory = new TwoPhaseHASCOFactory();
		hascoFactory.setHascoFactory(new HASCOViaFDAndBestFirstWithRandomCompletionsFactory(this.config.seed(),
				this.config.numberOfRandomCompletions(), this.config.timeoutForCandidateEvaluation(),
				this.config.timeoutForNodeEvaluation()));

		hascoFactory.setConfig(this.config);
		this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
		this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
		this.optimizingFactory.setTimeout(this.config.timeout(), TimeUnit.SECONDS);
		this.optimizingFactory.registerListener(this);
		this.optimizingFactory.init();

		/* set state to active */
		this.state = EAlgorithmState.ACTIVE;
		return new AlgorithmInitializedEvent(this.getId());
	}

	private AlgorithmEvent search() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		/* train the classifier returned by the optimizing factory */
		long startOptimizationTime = System.currentTimeMillis();
		this.setSelectedPipeline(this.optimizingFactory.call());
		this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
		long startBuildTime = System.currentTimeMillis();
		try {
			this.selectedPipeline.buildClassifier(this.data);
		} catch (Exception e) {
			throw new AlgorithmException(e, "Coul not build the selected pipeline");
		}
		long endBuildTime = System.currentTimeMillis();
		this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
		this.state = EAlgorithmState.INACTIVE;
		return new AlgorithmFinishedEvent(this.getId());
	}

	@Override
	public void setLoggerName(final String name) {
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
		throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
	}

	@Override
	public String[] getOptions() {
		throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
	}

	public void setData(final DataSet data) {
		this.data = data;
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(final Instances data) {
		return new WekaPipelineValidityCheckingNodeEvaluator(this.getComponents(), data);
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeoutInS) {
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		this.config.setProperty(MLPlanFEWekaClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeoutInS * 1000));
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	@Override
	public void setMaxNumThreads(final int maxNumberOfThreads) {
		this.setNumCPUs(maxNumberOfThreads);
	}

	@Override
	public void setTimeout(final long timeout, final TimeUnit timeUnit) {
		this.setTimeout(new TimeOut(timeout, timeUnit));
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		this.config.setProperty(IAlgorithmConfig.K_TIMEOUT, "" + timeout.milliseconds());
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(this.config.timeout(), TimeUnit.MILLISECONDS);
	}

	@Override
	public IAlgorithmConfig getConfig() {
		return this.config;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
