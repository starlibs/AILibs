package autofe.algorithm.hasco;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.algorithm.TrainingException;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.multiclass.MLPlanWekaBuilder;
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

	private Timeout feTimeOut; // timeout for the feature engineering phase
	private Timeout amlTimeOut; // timeout for the automl phase
	private Timeout evalTimeOut; // timeout for single node evaluation

	private Random rand;
	private File componentFile;

	private HASCOFeatureEngineeringConfig config;
	private long seed;

	private double internalAutoFEScore;
	private double internalMlPlanScore;

	public AutoFEMLTwoPhase(final HASCOFeatureEngineeringConfig hascoFEConfig, final int cpus,
			final String benchmarkType, final double subsampleRatio, final double mlplanSubsampleRatioFactor,
			final int minInstances, final long seed, final Timeout feTimeOut, final Timeout amlTimeOut,
			final Timeout evalTimeOut, final int maxPipelineSize) {

		this.cpus = cpus;
		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;
		this.maxPipelineSize = maxPipelineSize;
		this.rand = new Random(seed);
		this.benchmarkType = benchmarkType;

		this.feTimeOut = feTimeOut;
		this.amlTimeOut = amlTimeOut;
		this.evalTimeOut = evalTimeOut;

		logger.debug("Load components...");
		this.componentFile = new File("model/catalano/catalano.json");

		this.config = hascoFEConfig;
		this.seed = seed;
	}

	@Override
	public void buildClassifier(final DataSet data) throws TrainingException {
		try {
			// Search for AutoFE pipeline
			AbstractHASCOFEObjectEvaluator benchmark;
			switch (this.benchmarkType) {
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
			benchmark.setAdapter(this.getAdapter());
			benchmark.setEvalTable(this.getEvalTable());
			benchmark.setExperimentID(this.getExperimentID());

			long[] shape = data.getIntermediateInstances().get(0).shape();
			HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(this.componentFile, new FilterPipelineFactory(shape),
					benchmark, this.config);
			hascoFE.setTimeoutForNodeEvaluation((int) this.evalTimeOut.seconds());
			hascoFE.setTimeoutForSingleSolutionEvaluation((int) this.evalTimeOut.seconds());
			hascoFE.setTimeout((int) this.feTimeOut.seconds(), TimeUnit.SECONDS);
			hascoFE.setNumCPUs(this.cpus);
			hascoFE.setMaxPipelineSize(this.maxPipelineSize);
			hascoFE.setMinInstances(this.minInstances);
			hascoFE.setSubsamplingRatio(this.subsampleRatio);
			logger.info("Run 1st AutoFEML phase engineering features from the provided data using {} as a benchmark.",
					benchmark.getClass().getName());
			FilterPipeline solution = hascoFE.build(data.copy());
			this.internalAutoFEScore = hascoFE.getInternalValidationErrorOfSelectedClassifier();

			logger.info("Finished 1st AutoFEML phase. Found solution {} with score {}.", solution,
					this.internalAutoFEScore);

			logger.info("Prepare the dataset for the 2nd phase...");
			logger.info(
					"Subsampling with ratio {}, {} min instances and mlplan ratio factor {}. Num original instances and attributes: {} / {}...",
					this.subsampleRatio, this.minInstances, this.mlplanSubsampleRatioFactor,
					data.getInstances().numInstances(), data.getInstances().numAttributes());
			logger.info("Seed: {}", this.seed);
			DataSet dataForMLPlan = DataSetUtils.subsample(data, this.subsampleRatio, this.minInstances, this.rand,
					this.mlplanSubsampleRatioFactor);
			logger.info("Subsampled data.");
			DataSet transformedDataset = solution.applyFilter(dataForMLPlan, false);
			transformedDataset.updateInstances();
			Instances wekaDataset = transformedDataset.getInstances();
			logger.info("Done transforming the dataset for 2nd phase.");

			MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
			builder.withTimeOut(this.amlTimeOut).withCandidateEvaluationTimeOut(this.evalTimeOut).withNodeEvaluationTimeOut(this.evalTimeOut).withNumCpus(this.cpus);

			MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
			mlplan.setLoggerName("mlplan");
			mlplan.registerListener(this);

			logger.info("Run 2nd AutoFEML phase performing AutoML");
			mlplan.buildClassifier(wekaDataset);
			this.internalMlPlanScore = mlplan.getInternalValidationErrorOfSelectedClassifier();
			logger.info("Finished 2nd AutoFEML phase. Found solution {}.", mlplan.getSelectedWekaClassifier());

			this.setSelectedPipeline(new AutoFEWekaPipeline(solution, mlplan.getSelectedWekaClassifier()));

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
		return this.internalAutoFEScore;
	}

	public double getInternalMlPlanScore() {
		return this.internalMlPlanScore;
	}
}
