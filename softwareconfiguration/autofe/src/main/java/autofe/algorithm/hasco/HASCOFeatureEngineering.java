package autofe.algorithm.hasco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.aeonbits.owner.ConfigFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final String id = getClass().getName() + "-" + System.currentTimeMillis();
    private final File componentFile;
    private final Collection<Component> components;
    private final FilterPipelineFactory factory;
    private FilterPipeline selectedPipeline;
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
            throw new NoSuchElementException("Could not find a next element due to the following exception:" + e.getMessage());
        }
    }

    @Override
    public AlgorithmEvent nextWithException() throws AlgorithmException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
        switch (state) {
            case created:
                return setupSearch();
            case active:
                return search();
            default:
                throw new IllegalStateException("Cannot do anything in state " + state);
        }
    }

    private AlgorithmEvent setupSearch() throws AlgorithmException {

        /* check whether data has been set */
        if (data == null) {
            throw new IllegalArgumentException("Data to work on is still null");
        }

        /* check number of CPUs assigned */
        if (config.cpus() < 1) {
            throw new IllegalStateException("Cannot generate search where number of CPUs is " + config.cpus());
        }

        /* Subsample dataset to reduce computational effort. */
        logger.info("Subsampling with ratio {} and {} min instances. Num original instances and attributes: {} / {}...",
                config.subsamplingRatio(), config.minInstances(), data.getInstances().numInstances(),
                data.getInstances().numAttributes());
        DataSet dataForFE = DataSetUtils.subsample(data, config.subsamplingRatio(), config.minInstances(), new Random(config.randomSeed()));
        logger.info("Finished subsampling.");

        // Apply subsampling of images
        if (LongStream.of(dataForFE.getIntermediateInstances().get(0).shape()).reduce(1, (a, b) -> a * b) > (100 * 100)) {
            DataSetUtils.reduceHighDimensionalityByPoolingInPlace(dataForFE);
        }

        /* communicate the parameters with which AutoFE will run */
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

        /* configure and start optimizing factory */
        OptimizingFactoryProblem<RefinementConfiguredSoftwareConfigurationProblem<Double>, FilterPipeline, Double> optimizingFactoryProblem =
                new OptimizingFactoryProblem<>(factory, problem);
        OnePhaseHASCOFactory hascoFactory = new OnePhaseHASCOFactory(config);
        hascoFactory.withAlgorithmConfig(config);
        hascoFactory.setProblemInput(problem);

        hascoFactory.setSearchProblemTransformer(
                new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(nodeEvaluator,
                        null, config.randomSeed(), config.numberOfRandomCompletions(),
                        config.timeoutForCandidateEvaluation(), config.timeoutForNodeEvaluation()));

        optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
        optimizingFactory.setLoggerName(loggerName + ".2phasehasco");
        optimizingFactory.setTimeout(config.timeout(), TimeUnit.SECONDS);
        optimizingFactory.registerListener(this);
        optimizingFactory.setNumCPUs(config.cpus());
        optimizingFactory.init();

        /* set state to active */
        state = AlgorithmState.active;
        return new AlgorithmInitializedEvent(getId());
    }

    private AlgorithmEvent search() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
        /* train the classifier returned by the optimizing factory */
        long startOptimizationTime = System.currentTimeMillis();
        selectedPipeline = optimizingFactory.call();
        internalValidationErrorOfSelectedClassifier = optimizingFactory.getPerformanceOfObject();
        long startBuildTime = System.currentTimeMillis();
        long endBuildTime = System.currentTimeMillis();
        logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms",
                endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
        state = AlgorithmState.inactive;
        return new AlgorithmFinishedEvent(getId());
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
        this.setNumCPUs(maxNumberOfThreads);
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
