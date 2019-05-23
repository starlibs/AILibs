package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

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
import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import hasco.core.HASCOSolutionCandidate;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import weka.core.Instances;

public class AutoFEMLTwoPhase extends AbstractAutoFEMLClassifier {

	private static final Logger logger = LoggerFactory.getLogger(AutoFEMLTwoPhase.class);

	private String benchmarkType;
	private final double subsampleRatio;
	private final double mlplanSubsampleRatioFactor;
	private int minInstances;
	private int maxPipelineSize;
	private int cpus = 1;

	private TimeOut feTimeOut; // timeout for the feature engineering phase
	private TimeOut amlTimeOut; // timeout for the automl phase
	private TimeOut evalTimeOut; // timeout for single node evaluation

	private Random rand;
	private File componentFile;
	private boolean enableVisualization = false;

	private HASCOFeatureEngineeringConfig config;
	private long seed;

	private double internalAutoFEScore;
	private double internalMlPlanScore;

	// TODO: Subscribe
	// private FilterPipelineFactory fpFactory;
	// private WEKAPipelineFactory mlPipeFactory;

	public AutoFEMLTwoPhase(final HASCOFeatureEngineeringConfig hascoFEConfig, final int cpus,
			final String benchmarkType, final double subsampleRatio, final double mlplanSubsampleRatioFactor,
			final int minInstances, final long seed, final TimeOut feTimeOut, final TimeOut amlTimeOut,
			final TimeOut evalTimeOut, final int maxPipelineSize) throws IOException {

		this.cpus = cpus;
		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;
		this.maxPipelineSize = maxPipelineSize;
		rand = new Random(seed);
		this.benchmarkType = benchmarkType;

		// TODO: Use timeouts
		this.feTimeOut = feTimeOut;
		this.amlTimeOut = amlTimeOut;
		this.evalTimeOut = evalTimeOut;

		logger.debug("Load components...");
		componentFile = new File("model/catalano/catalano.json");

		config = hascoFEConfig;
		this.seed = seed;
	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {

		// Search for AutoFE pipeline
		AbstractHASCOFEObjectEvaluator benchmark = null;
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

		// Old
		// HASCOSupervisedML.REQUESTED_INTERFACE = "FilterPipeline";
		// HASCOImageFeatureEngineering hasco = new
		// HASCOImageFeatureEngineering(this.componentLoader);
		// hasco.setNumberOfCPUs(this.cpus);
		// hasco.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		// hasco.setTimeout((int) this.feTimeOut.seconds());
		//
		// // setup factory for filter pipelines
		// FilterPipelineFactory factory = new
		// FilterPipelineFactory(data.getIntermediateInstances().get(0).shape());
		// hasco.setFactory(factory);
		//
		// /* Setup node evaluators */
		// AutoFEPreferredNodeEvaluator nodeEvaluator = new
		// AutoFEPreferredNodeEvaluator(
		// this.componentLoader.getComponents(), factory, this.maxPipelineSize);
		// hasco.setPreferredNodeEvaluator(nodeEvaluator);
		//
		// benchmark.setData(dataForFE);
		// benchmark.setAdapter(this.getAdapter());
		// benchmark.setEvalTable(this.getEvalTable());
		// benchmark.setExperimentID(this.getExperimentID());
		// hasco.setBenchmark(benchmark);
		// hasco.enableVisualization(this.enableVisualization);

		// logger.info("Run 1st AutoFEML phase engineering features from the provided
		// data using {} as a benchmark.",
		// benchmark.getClass().getName());
		/* Run feature engineering phase */
		// hasco.gatherSolutions(this.feTimeOut);
		//
		// HASCOClassificationMLSolution<FilterPipeline> solution =
		// hasco.getCurrentlyBestSolution();
		// logger.info("Finished 1st AutoFEML phase. Found solution {} with score {} and
		// time {}ms to compute the score.",
		// solution.getSolution(), solution.getScore(),
		// solution.getTimeToComputeScore());

		System.gc();

		logger.info("Prepare the dataset for the 2nd phase...");
		logger.info(
				"Subsampling with ratio {}, {} min instances and mlplan ratio factor {}. Num original instances and attributes: {} / {}...",
				subsampleRatio, minInstances, mlplanSubsampleRatioFactor,
				data.getInstances().numInstances(), data.getInstances().numAttributes());
		logger.info("Seed: " + seed);
		DataSet dataForMLPlan = DataSetUtils.subsample(data, subsampleRatio, minInstances, rand,
				mlplanSubsampleRatioFactor);
		logger.info("Subsampled data.");
		DataSet transformedDataset = solution.applyFilter(dataForMLPlan, false);
		transformedDataset.updateInstances();
		Instances wekaDataset = transformedDataset.getInstances();
		logger.info("Done transforming the dataset for 2nd phase.");

		// HASCOSupervisedML.REQUESTED_INTERFACE = "AbstractClassifier";
		// MLPlanWekaClassifier mlplan = new MLPlanWekaClassifier();
		// mlplan.setNumberOfCPUs(this.cpus);
		// mlplan.setTimeout((int) this.amlTimeOut.seconds());
		// mlplan.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		// mlplan.enableVisualization(this.enableVisualization);
		// mlplan.registerListener(this);

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
	}

	public void setCPUs(final int cpus) {
		this.cpus = cpus;
	}

	@Subscribe
	@Override
	public void rcvHASCOSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> e) throws Exception {
		// if (this.adapter != null) {
		// AutoFEWekaPipeline pipe = factory
		// .getComponentInstantiation(e.getSolutionCandidate().getComponentInstance().getComponent().get);
		//
		// Map<String, Object> eval = new HashMap<>();
		// eval.put("run_id", this.experimentID);
		// eval.put("preprocessor", pipe.getFilterPipeline().toString());
		// eval.put("classifier", pipe.getMLPipeline().toString());
		// eval.put("errorRate", e.getSolutionCandidate().getScore());
		// eval.put("time_train",
		// e.getSolutionCandidate().getTimeToEvaluateCandidate());
		// eval.put("time_predict", -1);
		// try {
		// this.adapter.insert(this.evalTable, eval);
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }
		// }
		logger.debug("Subscribing solution candidate found events is not implemented yet in TwoPhase AutoFEML.");
	}

	public double getInternalAutoFEScore() {
		return internalAutoFEScore;
	}

	public double getInternalMlPlanScore() {
		return internalMlPlanScore;
	}
}
