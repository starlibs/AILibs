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
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.SemanticNodeEvaluator;
import hasco.core.HASCO;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.DefaultPathPriorizingPredicate;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class HASCOFeatureEngineering
		implements CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<DataSet, FilterPipeline> {

	private final HASCOFeatureEngineeringConfig config;

	/** Logger for controlled output */
	private static Logger logger = LoggerFactory.getLogger(HASCOFeatureEngineering.class);

	/**
	 * Logger name that can be used to customize logging outputs in a more
	 * convenient way.
	 */
	private String loggerName;

	/* new */
	private final File componentFile;
	private final Collection<Component> components;
	private final FilterPipelineFactory factory;
	private FilterPipeline selectedPipeline;
	private final EventBus eventBus = new EventBus();
	private OnePhaseHASCOFactory hascoFactory;
	private OptimizingFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, Double> optimizingFactory;
	private AlgorithmState state = AlgorithmState.created;
	private DataSet data = null;
	private final AbstractHASCOFEObjectEvaluator benchmark;
	private double internalValidationErrorOfSelectedClassifier;

	public HASCOFeatureEngineering(final File componentFile, final FilterPipelineFactory factory,
			final AbstractHASCOFEObjectEvaluator benchmark, HASCOFeatureEngineeringConfig config) throws IOException {
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.benchmark = benchmark;
		this.factory = factory;
		this.config = config;
	}

	// TODO: Change interface?
	public FilterPipeline build(final DataSet data) throws Exception {
		this.setData(data);
		return this.call();
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
	public AlgorithmEvent nextWithException() throws Exception {
		// TODO Auto-generated method stub
		switch (this.state) {
		case created: {
			/* check whether data has been set */
			if (this.data == null) {
				throw new IllegalArgumentException("Data to work on is still null");
			}

			/* check number of CPUs assigned */
			if (this.config.cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
			}

			/* Subsample dataset to reduce computational effort. */
			DataSet dataForFE = DataSetUtils.subsample(data, this.config.subsamplingRatio(), this.config.minInstances(),
					new Random(this.config.randomSeed()));

			/* communicate the parameters with which AutoFE will run */
			// TODO
			logger.info("Starting HASCOImageFeatureEngineering with {} cpus, max pipeline size of {}.",
					this.config.cpus(), this.config.maxPipelineSize());

			/* create HASCO problem */
			this.benchmark.setData(dataForFE);
			IObjectEvaluator<ComponentInstance, Double> wrappedBenchmark = c -> this.benchmark
					.evaluate(this.factory.getComponentInstantiation(c));
			AutoFEPreferredNodeEvaluator nodeEvaluator = new AutoFEPreferredNodeEvaluator(this.components, factory,
					this.config.maxPipelineSize());
			RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(
					this.componentFile, "FilterPipeline", wrappedBenchmark);

			// TwoPhaseSoftwareConfigurationProblem problem = new
			// TwoPhaseSoftwareConfigurationProblem(this.componentFile,
			// "FilterPipeline", wrappedBenchmark, wrappedBenchmark);

			/* configure and start optimizing factory */
			OptimizingFactoryProblem<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(
					this.factory, problem);
			this.hascoFactory = new OnePhaseHASCOFactory(this.config);
			// TODO: dataShownToSearch

			this.hascoFactory.setProblemInput(problem);

			// TODO: Test
			// INodeEvaluator<TFDNode, Double> finalNE = new
			// AlternativeNodeEvaluator<TFDNode, Double>(
			// this.getSemanticNodeEvaluator(this.data.getInstances()), nodeEvaluator);

			DefaultPathPriorizingPredicate<TFDNode, String> prioritizingPredicate = new DefaultPathPriorizingPredicate<>();
			this.hascoFactory.setSearchProblemTransformer(
					new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(nodeEvaluator,
							prioritizingPredicate, this.config.randomSeed(), this.config.randomCompletions(),
							this.config.timeoutForCandidateEvaluation(), this.config.timeoutForNodeEvaluation()));

			prioritizingPredicate.setHasco(this.hascoFactory.getAlgorithm());
			this.hascoFactory.setPriorizingPredicate(prioritizingPredicate);

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
			// TODO: Is this necessary?
			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.selectedPipeline = this.optimizingFactory.call();
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			// this.selectedClassifier.buildClassifier(this.data);
			long endBuildTime = System.currentTimeMillis();
			logger.info(
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
	public FilterPipeline call() throws Exception {
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
	public void registerListener(Object listener) {
		this.optimizingFactory.registerListener(listener);
	}

	@Override
	public int getNumCPUs() {
		return this.config.cpus();
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
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_TIMEOUT, String.valueOf((int) (timeout * factor)));
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
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		if (numberOfCPUs < 1) {
			throw new IllegalArgumentException("Need to work with at least one CPU");
		}
		if (numberOfCPUs > Runtime.getRuntime().availableProcessors()) {
			logger.warn("Warning, configuring {} CPUs where the system has only {}", numberOfCPUs,
					Runtime.getRuntime().availableProcessors());
		}
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
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

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Subscribe
	public void receiveSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> event) {
		HASCOSolutionCandidate<Double> solution = event.getSolutionCandidate();
		try {
			logger.info("Received new solution {} with score {} and evaluation time {}ms",
					this.factory.getComponentInstantiation(solution.getComponentInstance()), solution.getScore(),
					solution.getTimeToEvaluateCandidate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.eventBus.post(event);
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		this.eventBus.register(listener);
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (this.state == AlgorithmState.created) {
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

		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(new Random().nextInt() * 1000),
				usedDataSetSize);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, dataset));
		}
		logger.info("Finished intermediate calculations.");
		DataSet originDataSet = new DataSet(split.get(0), intermediate);

		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);
		HASCOFeatureEngineering hascoImageFE = new HASCOFeatureEngineering(new File("model/catalano/catalano.json"),
				new FilterPipelineFactory(intermediate.get(0).shape()), null, config);

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
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH,
				String.valueOf(timeoutInS * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeoutInS) {
		this.config.setProperty(HASCOFeatureEngineeringConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE,
				String.valueOf(timeoutInS * 1000));
	}

	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data) {
		return new SemanticNodeEvaluator(this.components, data);
	}
}
