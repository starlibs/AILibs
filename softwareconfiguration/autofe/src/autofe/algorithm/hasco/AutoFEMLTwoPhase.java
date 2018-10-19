package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleObjectEvaluator;
import autofe.algorithm.hasco.evaluation.LDAObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML.HASCOClassificationMLSolution;
import de.upb.crc901.mlplan.multiclass.weka.MLPlanWekaClassifier;
import hasco.serialization.ComponentLoader;
import jaicore.basic.TimeOut;
import weka.core.Instances;

public class AutoFEMLTwoPhase extends AbstractAutoFEMLClassifier {

	private static final Logger logger = LoggerFactory.getLogger(AutoFEMLTwoPhase.class);

	private String benchmarkType;
	private final double subsampleRatio;
	private int minInstances;
	private int maxPipelineSize;
	private int cpus = 1;

	private TimeOut feTimeOut; // timeout for the feature engineering phase
	private TimeOut amlTimeOut; // timeout for the automl phase
	private TimeOut evalTimeOut; // timeout for single node evaluation

	private AutoFEWekaPipeline selectedPipeline;

	private Random rand;
	private ComponentLoader componentLoader;
	private boolean enableVisualization = false;

	public AutoFEMLTwoPhase(final int cpus, final String benchmarkType, final double subsampleRatio,
			final int minInstances, final long seed, final TimeOut feTimeOut, final TimeOut amlTimeOut,
			final TimeOut evalTimeOut, final int maxPipelineSize) throws IOException {
		this.cpus = cpus;
		this.subsampleRatio = subsampleRatio;
		this.minInstances = minInstances;
		this.maxPipelineSize = maxPipelineSize;
		this.rand = new Random(seed);
		this.benchmarkType = benchmarkType;

		this.feTimeOut = feTimeOut;
		this.amlTimeOut = amlTimeOut;
		this.evalTimeOut = evalTimeOut;

		logger.debug("Load components...");
		this.componentLoader = new ComponentLoader(new File("model/catalano/catalano.json"));
	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {
		/* Subsample dataset to reduce computational effort. */
		double ratio = this.subsampleRatio;
		if (data.getInstances().size() * ratio < this.minInstances) {
			ratio = (double) this.minInstances / data.getInstances().size();
		}
		DataSet dataForFE = DataSetUtils.getStratifiedSplit(data, this.rand, this.subsampleRatio).get(0);
		logger.debug("Subsampling ratio is {} and means {} many instances.", ratio, dataForFE.getInstances().size());

		HASCOSupervisedML.REQUESTED_INTERFACE = "FilterPipeline";
		HASCOImageFeatureEngineering hasco = new HASCOImageFeatureEngineering(this.componentLoader);
		hasco.setNumberOfCPUs(this.cpus);
		hasco.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		hasco.setTimeout((int) this.feTimeOut.seconds());

		// setup feactory for filter pipelines
		FilterPipelineFactory factory = new FilterPipelineFactory(data.getIntermediateInstances().get(0).shape());
		hasco.setFactory(factory);

		/* Setup node evaluators */
		AutoFEPreferredNodeEvaluator nodeEvaluator = new AutoFEPreferredNodeEvaluator(
				this.componentLoader.getComponents(), factory, this.maxPipelineSize);
		hasco.setPreferredNodeEvaluator(nodeEvaluator);
		AbstractHASCOFEObjectEvaluator benchmark = null;
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
		}
		benchmark.setData(dataForFE);
		benchmark.setAdapter(this.getAdapter());
		benchmark.setEvalTable(this.getEvalTable());
		benchmark.setExperimentID(this.getExperimentID());
		hasco.setBenchmark(benchmark);
		hasco.enableVisualization(this.enableVisualization);

		logger.info("Run 1st AutoFEML phase engineering features from the provided data using {} as a benchmark.",
				benchmark.getClass().getName());
		/* Run feature engineering phase */
		hasco.gatherSolutions(this.feTimeOut);

		HASCOClassificationMLSolution<FilterPipeline> solution = hasco.getCurrentlyBestSolution();
		logger.info("Finished 1st AutoFEML phase. Found solution {} with score {} and time {}ms to compute the score.",
				solution.getSolution(), solution.getScore(), solution.getTimeToComputeScore());

		logger.info("Prepare the dataset for the 2nd phase...");
		DataSet transformedDataset = solution.getSolution().applyFilter(data, false);
		transformedDataset.updateInstances();
		Instances wekaDataset = transformedDataset.getInstances();
		logger.info("Done transforming the dataset for 2nd phase.");

		HASCOSupervisedML.REQUESTED_INTERFACE = "AbstractClassifier";
		MLPlanWekaClassifier mlplan = new MLPlanWekaClassifier();
		mlplan.setNumberOfCPUs(this.cpus);
		mlplan.setTimeout((int) this.amlTimeOut.seconds());
		mlplan.setTimeoutForSingleFEvaluation((int) this.evalTimeOut.milliseconds());
		mlplan.enableVisualization(this.enableVisualization);
		mlplan.registerListener(this);

		logger.info("Run 2nd AutoFEML phase performing AutoML");
		mlplan.buildClassifier(wekaDataset);
		logger.info("Finished 2nd AutoFEML phase. Found solution {}.", mlplan.getSelectedClassifier());

		this.setSelectedPipeline(new AutoFEWekaPipeline(solution.getSolution(), mlplan.getSelectedClassifier()));

		logger.info("Finished entire AutoFEML process.");
	}

	public void setCPUs(final int cpus) {
		this.cpus = cpus;
	}

	@Override
	public void enableVisualization(final boolean enableVisualization) {
		this.enableVisualization = enableVisualization;
	}
}
