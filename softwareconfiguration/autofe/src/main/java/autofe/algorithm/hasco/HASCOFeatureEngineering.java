package autofe.algorithm.hasco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.aeonbits.owner.ConfigFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.OptimizingFactory;
import ai.libs.hasco.optimizingfactory.OptimizingFactoryProblem;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hasco.serialization.UnresolvableRequiredInterfaceException;
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
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class HASCOFeatureEngineering implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, FilterPipeline> {

	private final HASCOFeatureEngineeringConfig config;

	/**
	 * Logger for controlled output
	 */
	private static Logger logger = LoggerFactory.getLogger(HASCOFeatureEngineering.class);

	/**
	 * Logger name that can be used to customize logging outputs in a more convenient way.
	 */
	private String loggerName;

	private static final String NOT_SUPPORTED_METHOD_MESSAGE = "Not supported yet.";

	/* new */
	private final String id = this.getClass().getName() + "-" + System.currentTimeMillis();
	private final File componentFile;
	private final Collection<Component> components;
	private final FilterPipelineFactory factory;
	private FilterPipeline selectedPipeline;
	private OptimizingFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, HASCOSolutionCandidate<Double>, Double> optimizingFactory;
	private EAlgorithmState state = EAlgorithmState.CREATED;
	private DataSet data = null;
	private final AbstractHASCOFEObjectEvaluator benchmark;
	private double internalValidationErrorOfSelectedClassifier;

	public HASCOFeatureEngineering(final File componentFile, final FilterPipelineFactory factory, final AbstractHASCOFEObjectEvaluator benchmark, final HASCOFeatureEngineeringConfig config) throws IOException {
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.benchmark = benchmark;
		this.factory = factory;
		this.config = config;
	}

	public FilterPipeline build(final DataSet data) throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		this.setData(data);
		return this.call();
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
			throw new NoSuchElementException("Could not find a next element due to the following exception:" + e.getMessage());
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
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
			throw new IllegalArgumentException("Data to work on is still null");
		}

		/* check number of CPUs assigned */
		if (this.config.cpus() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
		}

		/* Subsample dataset to reduce computational effort. */
		logger.info("Subsampling with ratio {} and {} min instances. Num original instances and attributes: {} / {}...",
				this.config.subsamplingRatio(), this.config.minInstances(), this.data.getInstances().numInstances(),
				this.data.getInstances().numAttributes());
		DataSet dataForFE = DataSetUtils.subsample(this.data, this.config.subsamplingRatio(), this.config.minInstances(), new Random(this.config.randomSeed()));
		logger.info("Finished subsampling.");

		// Apply subsampling of images
		if (LongStream.of(dataForFE.getIntermediateInstances().get(0).shape()).reduce(1, (a, b) -> a * b) > (100 * 100)) {
			DataSetUtils.reduceHighDimensionalityByPoolingInPlace(dataForFE);
		}

		/* communicate the parameters with which AutoFE will run */
		logger.info("Starting HASCOImageFeatureEngineering with {} cpus, max pipeline size of {}.", this.config.cpus(), this.config.maxPipelineSize());

		/* create HASCO problem */
		this.benchmark.setData(dataForFE);
		IObjectEvaluator<ComponentInstance, Double> wrappedBenchmark = c -> this.benchmark.evaluate(this.factory.getComponentInstantiation(c));
		AutoFEPreferredNodeEvaluator nodeEvaluator = new AutoFEPreferredNodeEvaluator(this.components, this.factory, this.config.maxPipelineSize());
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem;
		try {
			problem = new RefinementConfiguredSoftwareConfigurationProblem<>(this.componentFile, "FilterPipeline", wrappedBenchmark);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmException(e, "Couldn't create the problem.");
		}

		/* configure and start optimizing factory */
		OptimizingFactoryProblem<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, Double> optimizingFactoryProblem =
				new OptimizingFactoryProblem<>(this.factory, problem);
		OnePhaseHASCOFactory hascoFactory = new OnePhaseHASCOFactory(this.config);
		hascoFactory.withAlgorithmConfig(this.config);
		hascoFactory.setProblemInput(problem);

		hascoFactory.setSearchProblemTransformer(
				new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(nodeEvaluator,
						null, this.config.randomSeed(), this.config.numberOfRandomCompletions(),
						this.config.timeoutForCandidateEvaluation(), this.config.timeoutForNodeEvaluation()));

		this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
		this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
		this.optimizingFactory.setTimeout(this.config.timeout(), TimeUnit.SECONDS);
		this.optimizingFactory.registerListener(this);
		this.optimizingFactory.setNumCPUs(this.config.cpus());
		this.optimizingFactory.init();

		/* set state to active */
		this.state = EAlgorithmState.ACTIVE;
		return new AlgorithmInitializedEvent(this.getId());
	}

	private AlgorithmEvent search() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		/* train the classifier returned by the optimizing factory */
		long startOptimizationTime = System.currentTimeMillis();
		this.selectedPipeline = this.optimizingFactory.call();
		this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
		long startBuildTime = System.currentTimeMillis();
		long endBuildTime = System.currentTimeMillis();
		logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms",
				endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
		this.state = EAlgorithmState.INACTIVE;
		return new AlgorithmFinishedEvent(this.getId());
	}

	@Override
	public FilterPipeline call() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.selectedPipeline;
	}

	public void setData(final DataSet data) {
		this.data = data;
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
	public int getNumCPUs() {
		return this.config.cpus();
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_TIMEOUT, String.valueOf((int) (timeout.milliseconds())));
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(this.config.timeout(), TimeUnit.MILLISECONDS);
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return new Iterator<AlgorithmEvent>() {
			@Override
			public boolean hasNext() {
				return HASCOFeatureEngineering.this.hasNext();
			}

			@Override
			public AlgorithmEvent next() {
				try {
					return HASCOFeatureEngineering.this.nextWithException();
				} catch (Exception e) {
					throw new NoSuchElementException("Can not return next element due to: " + e.getMessage());
				}
			}
		};
	}

	@Override
	public void cancel() {
		throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		if (numberOfCPUs < 1) {
			throw new IllegalArgumentException("Need to work with at least one CPU");
		}
		if (numberOfCPUs > Runtime.getRuntime().availableProcessors()) {
			logger.warn("Warning, configuring {} CPUs where the system has only {}", numberOfCPUs, Runtime.getRuntime().availableProcessors());
		}
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
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

	@Override
	public Capabilities getCapabilities() {
		throw new UnsupportedOperationException();
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (this.state == EAlgorithmState.CREATED) {
			this.init();
		}
		HASCO hasco = ((HASCO) this.optimizingFactory.getOptimizer());
		return hasco.getGraphGenerator();
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (this.hasNext()) {
			e = this.next();
			if (e instanceof AlgorithmInitializedEvent) {
				return (AlgorithmInitializedEvent) e;
			}
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	public static List<Instances> generateRandomDataSets(final int dataset, final int maxSolutionCount, final int maxPipelineSize) throws Exception {

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(dataset);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);

		final double usedDataSetSize = DataSetUtils.getSplitRatioToUse(data);
		logger.debug("Using split ratio {}", usedDataSetSize);

		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random().nextInt() * 1000L, usedDataSetSize);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, dataset));
		}
		logger.info("Finished intermediate calculations.");
		DataSet originDataSet = new DataSet(split.get(0), intermediate);

		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);
		HASCOFeatureEngineering hascoImageFE = new HASCOFeatureEngineering(new File("model/catalano/catalano.json"), new FilterPipelineFactory(intermediate.get(0).shape()), null, config);

		// Calculate solution data sets
		List<Instances> result = new ArrayList<>();
		if (maxSolutionCount > 1) {
			result.add(originDataSet.getInstances());
		}

		int solCounter = result.size();
		while (hascoImageFE.hasNext() && solCounter < maxSolutionCount) {
			FilterPipeline pipe = hascoImageFE.call();

			// Discard empty or oversized pipelines
			if (pipe.getFilters() == null || pipe.getFilters().getItems().size() > maxPipelineSize) {
				continue;
			}

			logger.debug("Applying solution pipe {}.", pipe);

			result.add(pipe.applyFilter(originDataSet, true).getInstances());
			solCounter++;
		}

		logger.debug("Got all randomly generated data sets.");

		return result;
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeoutInS) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeoutInS * 1000));
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(final Instances data) {
		return new WekaPipelineValidityCheckingNodeEvaluator(this.components, data);
	}

	public void setSubsamplingRatio(final double subsamplingRatio) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.SUBSAMPLING_RATIO, String.valueOf(subsamplingRatio));
	}

	public void setMinInstances(final int minInstances) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.MIN_INSTANCES, String.valueOf(minInstances));
	}

	public void setMaxPipelineSize(final int maxPipelineSize) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.SELECTION_PORTION, String.valueOf(maxPipelineSize));
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
	public IAlgorithmConfig getConfig() {
		return this.config;
	}

	@Override
	public String getId() {
		return this.id;
	}
}
