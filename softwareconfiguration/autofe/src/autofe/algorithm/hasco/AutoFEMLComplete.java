package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.evaluation.AutoFEMLMCCVBenchmark;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML.HASCOClassificationMLSolution;
import de.upb.crc901.mlplan.multiclass.weka.WEKAPipelineFactory;
import hasco.serialization.ComponentLoader;
import jaicore.basic.TimeOut;

public class AutoFEMLComplete extends AbstractAutoFEMLClassifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoFEMLComplete.class);

	private ComponentLoader componentLoader;
	private Random rand;

	private final TimeOut globalTimeOut;
	private final TimeOut evalTimeOut;
	private final int cpus;
	private final int maxPipelineSize;
	private final long seed;
	
	/* Subsampling parameters */
	private final double subsampleRatio;
	private final double mlplanSubsampleRatioFactor;
	private int minInstances;

	private boolean enableVisualization = false;

	public AutoFEMLComplete(final int cpus, final long seed, final TimeOut globalTimeOut, final TimeOut evalTimeOut,
			final int maxPipelineSize, final double subsampleRatio,	final double mlplanSubsampleRatioFactor, 
			final int minInstances) throws IOException {
		
		this.componentLoader = new ComponentLoader(new File("model/MLPlanFEWeka.json"));
		this.rand = new Random(seed);
		this.globalTimeOut = globalTimeOut;
		this.evalTimeOut = evalTimeOut;
		this.cpus = cpus;
		this.maxPipelineSize = maxPipelineSize;
		this.seed = seed;
		
		this.subsampleRatio = subsampleRatio;
		this.mlplanSubsampleRatioFactor = mlplanSubsampleRatioFactor;
		this.minInstances = minInstances;
	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {
		/* Subsample dataset to reduce computational effort. */
		DataSet dataForComplete = DataSetUtils.subsample(data, this.subsampleRatio, this.minInstances, this.rand, this.mlplanSubsampleRatioFactor);
		
		LOGGER.info("Setup MLPlanWithFeatureEngineering...");
		HASCOSupervisedML.REQUESTED_INTERFACE = "AutoFEMLPipeline";
		MLPlanWithFeatureEngineering mlplan = new MLPlanWithFeatureEngineering(this.componentLoader);
		mlplan.setNumberOfCPUs(this.cpus);
		mlplan.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		mlplan.setTimeout((int) this.globalTimeOut.seconds());

		LOGGER.info("Setup AutoFEWekaPipelineFactory...");
		AutoFEWekaPipelineFactory factory = new AutoFEWekaPipelineFactory(
				new FilterPipelineFactory(data.getIntermediateInstances().get(0).shape()), new WEKAPipelineFactory());
		mlplan.setFactory(factory);

		LOGGER.debug("Create search/selection split...");
		List<DataSet> searchSelectSplit = DataSetUtils.getStratifiedSplit(dataForComplete, new Random(this.seed),
				mlplan.getConfig().selectionDataPortion());

		/* Setup node evaluators */
		LOGGER.info("Setup node and object evaluators...");
		AutoFEMLPreferredNodeEvaluator nodeEvaluator = new AutoFEMLPreferredNodeEvaluator(
				this.componentLoader.getComponents(), factory, this.maxPipelineSize);
		mlplan.setPreferredNodeEvaluator(nodeEvaluator);

		AutoFEMLMCCVBenchmark benchmark = new AutoFEMLMCCVBenchmark(searchSelectSplit.get(1), this.seed,
				mlplan.getConfig().searchMCIterations(), mlplan.getConfig().searchDataPortion());
		mlplan.setBenchmark(benchmark);
		benchmark.setAdapter(this.getAdapter());
		benchmark.setEvalTable(this.getEvalTable());
		benchmark.setExperimentID(this.getExperimentID());

		AutoFEMLMCCVBenchmark selectionBenchmark = new AutoFEMLMCCVBenchmark(dataForComplete, this.seed,
				mlplan.getConfig().searchMCIterations(), mlplan.getConfig().searchDataPortion());
		mlplan.setSelectionPhaseEvaluator(selectionBenchmark);
		mlplan.enableVisualization(this.enableVisualization);

		/* Run feature engineering phase */
		LOGGER.info("Run ML-Plan including Feature Engineering...");
		mlplan.gatherSolutions(this.globalTimeOut);

		HASCOClassificationMLSolution<AutoFEWekaPipeline> solution = mlplan.getCurrentlyBestSolution();
		LOGGER.info(
				"Found solution " + solution.getSolution().toString() + " with internal score: " + solution.getScore()
						+ " and it took " + solution.getTimeToComputeScore() + "ms to compute its score.");
		this.setSelectedPipeline(solution.getSolution());
		this.getSelectedPipeline().buildClassifier(dataForComplete);

	}

	@Override
	public void enableVisualization(final boolean enableVisualization) {
		this.enableVisualization = enableVisualization;
	}

}
