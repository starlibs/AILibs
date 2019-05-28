package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import jaicore.ml.core.exception.TrainingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
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
    private AlgorithmState state = AlgorithmState.created;
    private MLPlanFEWekaClassifierConfig config;
    private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, AutoFEWekaPipeline, HASCOSolutionCandidate<Double>, Double> optimizingFactory;
    private final ISplitBasedClassifierEvaluator<Double> benchmark;
    private final AutoFEWekaPipelineFactory factory;
    private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;

    private double internalValidationErrorOfSelectedClassifier;
    private final String id = getClass().getName() + "-" + System.currentTimeMillis();

    public AutoFEMLComplete(final long seed, final double subsampleRatio, final double mlplanSubsampleRatioFactor, final int minInstances, final MLPlanFEWekaClassifierConfig config, final AutoFEWekaPipelineFactory factory)
            throws IOException {

        componentFile = new File("model/MLPlanFEWeka.json");
        components = new ComponentLoader(componentFile).getComponents();

        rand = new Random(seed);

        this.subsampleRatio = subsampleRatio;
        this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
        this.minInstances = minInstances;

        this.config = config;
        benchmark = new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss());
        this.factory = factory;
        preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(getComponents(), FileUtil.readFileAsList(this.config.preferredComponents()));

    }

    @Override
    public void buildClassifier(final DataSet data) throws TrainingException {
        setData(data);
        try {
            call();
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
        return state != AlgorithmState.inactive;
    }

    @Override
    public AlgorithmEvent next() {
        try {
            return nextWithException();
        } catch (Exception e) {
            throw new NoSuchElementException("Can not return next element due to: " + e.getMessage());
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
        throw new UnsupportedOperationException(NOT_SUPPORTED_METHOD_MESSAGE);
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
        Instances dataShownToSearch;
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
        double blowUpInSelectionPhase = MathExt.round(
                1f / NUMBER_OF_MC_FOLDS_IN_SEARCH * (NUMBER_OF_MC_FOLDS_IN_SELECTION / (double) NUMBER_OF_MC_ITERATIONS_IN_SEARCH), 2);
        double blowUpInPostprocessing = MathExt.round((1 / (1 - config.dataPortionForSelection())) / NUMBER_OF_MC_FOLDS_IN_SELECTION, 2);
        config.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
        config.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

        logger.info("Starting AutoFEMLComplete search.");
        logger.info("Using the following preferred node evaluator: {}", preferredNodeEvaluator);

        /* create HASCO problem */
        IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(benchmark, NUMBER_OF_MC_ITERATIONS_IN_SEARCH, dataShownToSearch, NUMBER_OF_MC_FOLDS_IN_SEARCH, config.seed());
        IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> {
            try {
                return searchBenchmark.evaluate(factory.getComponentInstantiation(c));
            } catch (ComponentInstantiationFailedException e1) {
                throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e1);
            }
        };
        IObjectEvaluator<Classifier, Double> selectionBenchmark = object -> {

            logger.info("Evaluating object {}...", object);

            /* first conduct MCCV */
            MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(benchmark, NUMBER_OF_MC_ITERATIONS_IN_SELECTION, dataForComplete.getInstances(), NUMBER_OF_MC_FOLDS_IN_SELECTION, config.seed());
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
        TwoPhaseHASCOFactory hascoFactory = new TwoPhaseHASCOFactory();

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

    private AlgorithmEvent search() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
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

    public double getInternalValidationErrorOfSelectedClassifier() {
        return internalValidationErrorOfSelectedClassifier;
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
