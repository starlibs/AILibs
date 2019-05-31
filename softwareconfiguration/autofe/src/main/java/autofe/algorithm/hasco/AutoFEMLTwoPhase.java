package autofe.algorithm.hasco;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.ml.core.exception.TrainingException;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlanWekaBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.evaluation.COEDObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleObjectEvaluator;
import autofe.algorithm.hasco.evaluation.LDAObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.core.Instances;

public class AutoFEMLTwoPhase extends AbstractAutoFEMLClassifier {

    private static final Logger logger = LoggerFactory.getLogger(AutoFEMLTwoPhase.class);

    private String benchmarkType;
    private final double subsampleRatio;
    private final double mlplanSubsampleRatioFactor;
    private int minInstances;
    private int maxPipelineSize;
    private int cpus;

    private TimeOut feTimeOut; // timeout for the feature engineering phase
    private TimeOut amlTimeOut; // timeout for the automl phase
    private TimeOut evalTimeOut; // timeout for single node evaluation

    private Random rand;
    private File componentFile;

    private HASCOFeatureEngineeringConfig config;
    private long seed;

    private double internalAutoFEScore;
    private double internalMlPlanScore;

    public AutoFEMLTwoPhase(final HASCOFeatureEngineeringConfig hascoFEConfig, final int cpus,
                            final String benchmarkType, final double subsampleRatio, final double mlplanSubsampleRatioFactor,
                            final int minInstances, final long seed, final TimeOut feTimeOut, final TimeOut amlTimeOut,
                            final TimeOut evalTimeOut, final int maxPipelineSize) {

        this.cpus = cpus;
        this.subsampleRatio = subsampleRatio;
        this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
        this.minInstances = minInstances;
        this.maxPipelineSize = maxPipelineSize;
        rand = new Random(seed);
        this.benchmarkType = benchmarkType;

        this.feTimeOut = feTimeOut;
        this.amlTimeOut = amlTimeOut;
        this.evalTimeOut = evalTimeOut;

        logger.debug("Load components...");
        componentFile = new File("model/catalano/catalano.json");

        config = hascoFEConfig;
        this.seed = seed;
    }

    @Override
    public void buildClassifier(final DataSet data) throws TrainingException {
        try {
            // Search for AutoFE pipeline
            AbstractHASCOFEObjectEvaluator benchmark;
            switch (benchmarkType) {
                case "cluster":
                    benchmark = new ClusterObjectEvaluator();
                    break;
                case "lda":
                    benchmark = new LDAObjectEvaluator();
                    break;
                case "ensemble":
                    benchmark = new EnsembleObjectEvaluator();
                    break;
                case "coco":
                    benchmark = new COCOObjectEvaluator();
                    break;
                default:
                case "coed":
                    benchmark = new COEDObjectEvaluator();
                    break;
            }
            benchmark.setAdapter(getAdapter());
            benchmark.setEvalTable(getEvalTable());
            benchmark.setExperimentID(getExperimentID());

            long[] shape = data.getIntermediateInstances().get(0).shape();
            HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(componentFile, new FilterPipelineFactory(shape),
                    benchmark, config);
            hascoFE.setTimeoutForNodeEvaluation((int) evalTimeOut.seconds());
            hascoFE.setTimeoutForSingleSolutionEvaluation((int) evalTimeOut.seconds());
            hascoFE.setTimeout((int) feTimeOut.seconds(), TimeUnit.SECONDS);
            hascoFE.setNumCPUs(cpus);
            hascoFE.setMaxPipelineSize(maxPipelineSize);
            hascoFE.setMinInstances(minInstances);
            hascoFE.setSubsamplingRatio(subsampleRatio);
            logger.info("Run 1st AutoFEML phase engineering features from the provided data using {} as a benchmark.",
                    benchmark.getClass().getName());
            FilterPipeline solution = hascoFE.build(data.copy());
            internalAutoFEScore = hascoFE.getInternalValidationErrorOfSelectedClassifier();

            logger.info("Finished 1st AutoFEML phase. Found solution {} with score {}.", solution,
                    internalAutoFEScore);

            logger.info("Prepare the dataset for the 2nd phase...");
            logger.info(
                    "Subsampling with ratio {}, {} min instances and mlplan ratio factor {}. Num original instances and attributes: {} / {}...",
                    subsampleRatio, minInstances, mlplanSubsampleRatioFactor,
                    data.getInstances().numInstances(), data.getInstances().numAttributes());
            logger.info("Seed: {}", seed);
            DataSet dataForMLPlan = DataSetUtils.subsample(data, subsampleRatio, minInstances, rand,
                    mlplanSubsampleRatioFactor);
            logger.info("Subsampled data.");
            DataSet transformedDataset = solution.applyFilter(dataForMLPlan, false);
            transformedDataset.updateInstances();
            Instances wekaDataset = transformedDataset.getInstances();
            logger.info("Done transforming the dataset for 2nd phase.");

            MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
            builder.withTimeOut(amlTimeOut).withCandidateEvaluationTimeOut(evalTimeOut).withNodeEvaluationTimeOut(evalTimeOut).withNumCpus(cpus);

            MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
            mlplan.setLoggerName("mlplan");
            mlplan.registerListener(this);

            logger.info("Run 2nd AutoFEML phase performing AutoML");
            mlplan.buildClassifier(wekaDataset);
            internalMlPlanScore = mlplan.getInternalValidationErrorOfSelectedClassifier();
            logger.info("Finished 2nd AutoFEML phase. Found solution {}.", mlplan.getSelectedClassifier());

            setSelectedPipeline(new AutoFEWekaPipeline(solution, mlplan.getSelectedClassifier()));

            logger.info("Finished entire AutoFEML process.");
        } catch (Exception e) {
            final String failureMessage = "Could not build AutoFEMLTwoPhase classifier due to an exception.";
            logger.warn(failureMessage, e);
            throw new TrainingException(failureMessage, e);
        }
    }

    public void setCPUs(final int cpus) {
        this.cpus = cpus;
    }

    public double getInternalAutoFEScore() {
        return internalAutoFEScore;
    }

    public double getInternalMlPlanScore() {
        return internalMlPlanScore;
    }
}
