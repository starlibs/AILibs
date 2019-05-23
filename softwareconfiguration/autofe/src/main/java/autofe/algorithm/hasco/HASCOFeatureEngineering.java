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
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.aeonbits.owner.ConfigFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import hasco.core.HASCO;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
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
import jaicore.ml.WekaUtil;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class HASCOFeatureEngineering implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, FilterPipeline> {

	private final HASCOFeatureEngineeringConfig config;

	/** Logger for controlled output */
	private static Logger logger = LoggerFactory.getLogger(HASCOFeatureEngineering.class);

	/**
	 * Logger name that can be used to customize logging outputs in a more convenient way.
	 */
	private String loggerName;

	/* new */
	private final String id = getClass().getName() + "-" + System.currentTimeMillis();
	private final File componentFile;
	private final Collection<Component> components;
	private final FilterPipelineFactory factory;
	private FilterPipeline selectedPipeline;
	private final EventBus eventBus = new EventBus();
	private OnePhaseHASCOFactory hascoFactory;
	private OptimizingFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, HASCOSolutionCandidate<Double>, Double> optimizingFactory;
	private AlgorithmState state = AlgorithmState.created;
	private DataSet data = null;
	private final AbstractHASCOFEObjectEvaluator benchmark;
	private double internalValidationErrorOfSelectedClassifier;

	public HASCOFeatureEngineering(final File componentFile, final FilterPipelineFactory factory, final AbstractHASCOFEObjectEvaluator benchmark, final HASCOFeatureEngineeringConfig config) throws IOException {
		this.componentFile = componentFile;
		components = new ComponentLoader(componentFile).getComponents();
		this.benchmark = benchmark;
		this.factory = factory;
		this.config = config;
	}

	// TODO: Change interface?
	public FilterPipeline build(final DataSet data) throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		setData(data);
		return call();
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
	public AlgorithmEvent nextWithException() throws AlgorithmException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		// TODO Auto-generated method stub
		switch (state) {
		case created: {
			/* check whether data has been set */
			if (data == null) {
				throw new IllegalArgumentException("Data to work on is still null");
			}

			/* check number of CPUs assigned */
			if (config.cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + config.cpus());
			}

			/* Subsample dataset to reduce computational effort. */
			logger.info("Subsampling with ratio {} and {} min instances. Num original instances and attributes: {} / {}...", config.subsamplingRatio(), config.minInstances(), data.getInstances().numInstances(),
					data.getInstances().numAttributes());
			DataSet dataForFE = DataSetUtils.subsample(data, config.subsamplingRatio(), config.minInstances(), new Random(config.randomSeed()));
			logger.info("Finished subsampling.");

			// Apply subsampling of images
			if (LongStream.of(dataForFE.getIntermediateInstances().get(0).shape()).reduce(1, (a, b) -> a * b) > (100 * 100)) {
				DataSetUtils.reduceHighDimensionalityByPoolingInPlace(dataForFE);
			}

			/* communicate the parameters with which AutoFE will run */
			// TODO
			logger.info("Starting HASCOImageFeatureEngineering with {} cpus, max pipeline size of {}.", config.cpus(), config.maxPipelineSize());

			/* create HASCO problem */
			benchmark.setData(dataForFE);
			IObjectEvaluator<ComponentInstance, Double> wrappedBenchmark = c -> benchmark.evaluate(factory.getComponentInstantiation(c));
			AutoFEPreferredNodeEvaluator nodeEvaluator = new AutoFEPreferredNodeEvaluator(components, factory, config.maxPipelineSize());
			RefinementConfiguredSoftwareConfigurationProblem<Double> problem;
			try {
				problem = new RefinementConfiguredSoftwareConfigurationProblem<>(componentFile, "FilterPipeline", wrappedBenchmark);
			} catch (UnresolvableRequiredInterfaceException | IOException e) {
				throw new AlgorithmException(e, "Couldn't create the problem.");
			}

			// TwoPhaseSoftwareConfigurationProblem problem = new
			// TwoPhaseSoftwareConfigurationProblem(this.componentFile,
			// "FilterPipeline", wrappedBenchmark, wrappedBenchmark);

			/* configure and start optimizing factory */
			OptimizingFactoryProblem<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(factory, problem);
			hascoFactory = new OnePhaseHASCOFactory(config);
			// TODO: dataShownToSearch

			hascoFactory.setProblemInput(problem);

			// TODO: Test
			// INodeEvaluator<TFDNode, Double> finalNE = new
			// AlternativeNodeEvaluator<TFDNode, Double>(
			// this.getSemanticNodeEvaluator(this.data.getInstances()), nodeEvaluator);

			// DefaultPathPriorizingPredicate<TFDNode, String> prioritizingPredicate = new
			// DefaultPathPriorizingPredicate<>();
			hascoFactory.setSearchProblemTransformer(
					new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(nodeEvaluator, null, config.randomSeed(), config.numberOfRandomCompletions(), config.timeoutForCandidateEvaluation(), config.timeoutForNodeEvaluation()));

			// prioritizingPredicate.setHasco(this.hascoFactory.getAlgorithm());
			// this.hascoFactory.setPriorizingPredicate(prioritizingPredicate);

			optimizingFactory = new OptimizingFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, HASCOSolutionCandidate<Double>, Double>(optimizingFactoryProblem, null);
			optimizingFactory.setLoggerName(loggerName + ".2phasehasco");
			optimizingFactory.setTimeout(config.timeout(), TimeUnit.SECONDS);
			optimizingFactory.registerListener(this);
			optimizingFactory.setNumCPUs(config.cpus());
			optimizingFactory.init();

			/* set state to active */
			state = AlgorithmState.active;
			return new AlgorithmInitializedEvent(getId());
		}
		case active: {
			// TODO: Is this necessary?
			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			selectedPipeline = optimizingFactory.call();
			internalValidationErrorOfSelectedClassifier = optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			// this.selectedClassifier.buildClassifier(this.data);
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
	public FilterPipeline call() throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		while (hasNext()) {
			nextWithException();
		}
		return selectedPipeline;
	}

	public void setData(final DataSet data) {
		this.data = data;
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
	public int getNumCPUs() {
		return config.cpus();
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		config.setProperty(HASCOFeatureEngineeringConfig.K_TIMEOUT, String.valueOf((int) (timeout.milliseconds())));
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(config.timeout(), TimeUnit.MILLISECONDS);
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		if (numberOfCPUs < 1) {
			throw new IllegalArgumentException("Need to work with at least one CPU");
		}
		if (numberOfCPUs > Runtime.getRuntime().availableProcessors()) {
			logger.warn("Warning, configuring {} CPUs where the system has only {}", numberOfCPUs, Runtime.getRuntime().availableProcessors());
		}
		config.setProperty(HASCOFeatureEngineeringConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public void setLoggerName(final String name) {
		loggerName = name;
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

	@Override
	public Capabilities getCapabilities() {
		throw new UnsupportedOperationException();
	}

	@Subscribe
	public void receiveSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> event) {
		HASCOSolutionCandidate<Double> solution = event.getSolutionCandidate();
		try {
			logger.debug("Received new solution {} with score {} and evaluation time {}ms", factory.getComponentInstantiation(solution.getComponentInstance()), solution.getScore(), solution.getTimeToEvaluateCandidate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		eventBus.post(event);
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		eventBus.register(listener);
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (state == AlgorithmState.created) {
			init();
		}
		HASCO hasco = ((HASCO) optimizingFactory.getOptimizer());
		return hasco.getGraphGenerator();
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (hasNext()) {
			e = next();
			if (e instanceof AlgorithmInitializedEvent) {
				return (AlgorithmInitializedEvent) e;
			}
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return internalValidationErrorOfSelectedClassifier;
	}

	public static List<Instances> generateRandomDataSets(final int dataset, // final double usedDataSetSize,
			final int maxSolutionCount, final int maxPipelineSize, final int timeout, final int seed) throws Exception {

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(dataset);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);

		final double usedDataSetSize = DataSetUtils.getSplitRatioToUse(data);
		logger.debug("Using split ratio '" + usedDataSetSize + "'.");

		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random().nextInt() * 1000, usedDataSetSize);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, dataset));
		}
		logger.info("Finished intermediate calculations.");
		DataSet originDataSet = new DataSet(split.get(0), intermediate);

		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);
		HASCOFeatureEngineering hascoImageFE = new HASCOFeatureEngineering(new File("model/catalano/catalano.json"), new FilterPipelineFactory(intermediate.get(0).shape()), null, config);

		// HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(,
		// EvaluationUtils.getRandomNodeEvaluator(maxPipelineSize), new
		// DataSet(split.get(0), intermediate), null,
		// DataSetUtils.getInputShapeByDataSet(dataset));
		// hascoFE.setLoggerName("autofe");
		// hascoFE.runSearch(timeout);

		// Calculate solution data sets
		List<Instances> result = new ArrayList<>();
		if (maxSolutionCount > 1) {
			result.add(originDataSet.getInstances());
		}

		// logger.debug("Found solutions: " + hascoFE.getFoundClassifiers().toString());

		// List<HASCOFESolution> solutions = new
		// ArrayList<>(hascoFE.getFoundClassifiers());
		// logger.debug("Found " + solutions.size() + " solutions.");
		// Collections.shuffle(solutions, new Random(seed));
		//
		// Iterator<HASCOFESolution> solIt = solutions.iterator();

		int solCounter = result.size();
		while (hascoImageFE.hasNext() && solCounter < maxSolutionCount) {
			FilterPipeline pipe = hascoImageFE.call();

			// Discard empty or oversized pipelines
			if (pipe.getFilters() == null || pipe.getFilters().getItems().size() > maxPipelineSize) {
				continue;
			}

			logger.debug("Applying solution pipe " + pipe.toString());

			result.add(pipe.applyFilter(originDataSet, true).getInstances());
			solCounter++;
		}

		logger.debug("Got all randomly generated data sets.");

		return result;
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeoutInS) {
		config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeoutInS * 1000));
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(final Instances data) {
		return new WekaPipelineValidityCheckingNodeEvaluator(components, data);
	}

	public void setSubsamplingRatio(final double subsamplingRatio) {
		config.setProperty(HASCOFeatureEngineeringConfig.SUBSAMPLING_RATIO, String.valueOf(subsamplingRatio));
	}

	public void setMinInstances(final int minInstances) {
		config.setProperty(HASCOFeatureEngineeringConfig.MIN_INSTANCES, String.valueOf(minInstances));
	}

	public void setMaxPipelineSize(final int maxPipelineSize) {
		config.setProperty(HASCOFeatureEngineeringConfig.SELECTION_PORTION, String.valueOf(maxPipelineSize));
	}

	@Override
	public void setMaxNumThreads(final int maxNumberOfThreads) {

	}

	@Override
	public void setTimeout(final long timeout, final TimeUnit timeUnit) {
		setTimeout(new TimeOut(timeout, timeUnit));
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
