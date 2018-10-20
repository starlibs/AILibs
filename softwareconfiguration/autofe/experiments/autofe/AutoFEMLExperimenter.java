package autofe;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.AbstractAutoFEMLClassifier;
import autofe.algorithm.hasco.AutoFEMLComplete;
import autofe.algorithm.hasco.AutoFEMLTwoPhase;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class AutoFEMLExperimenter implements IExperimentSetEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoFEMLExperimenter.class);

	private static final AutoFEMLExperimenterConfig CONFIG = ConfigCache.getOrCreate(AutoFEMLExperimenterConfig.class);

	@Override
	public IExperimentSetConfig getConfig() {
		return CONFIG;
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter,
			final IExperimentIntermediateResultProcessor processor) throws Exception {
		Map<String, String> experiment = experimentEntry.getExperiment().getValuesOfKeyFields();
		LOGGER.info("Evaluate experiment: {}", experiment);

		double subsampleRatio = Double.valueOf(experiment.get("subsampleRatio"));
		long seed = Long.valueOf(experiment.get("seed"));
		long amlTimeout = Long.valueOf(experiment.get("amlTimeout"));
		long feTimeout = Long.valueOf(experiment.get("feTimeout"));
		long evalTimeout = Long.valueOf(experiment.get("evalTimeout"));
		int minInstances = Integer.valueOf(experiment.get("minInstances"));
		int maxPipelineSize = Integer.valueOf(experiment.get("maxPipelineSize"));

		LOGGER.info("Load dataset...");
		String datasetName = experiment.get("dataset");
		DataSet data;
		if (datasetName.startsWith("openml")) {
			int openmlID = Integer.valueOf(datasetName.substring(6));
			data = DataSetUtils.getDataSetByID(openmlID);
		} else {
			File datasetFolder = new File(CONFIG.getDatasetFolder().getAbsolutePath() + "/" + datasetName);
			data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		}

		LOGGER.info("Get stratified split of training and test data...");
		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(seed), .7);

		AbstractAutoFEMLClassifier autofeml;
		if (experiment.get("algorithm").equals("none")) {
			LOGGER.info("Execute AutoFEML as a complete process...");
			autofeml = new AutoFEMLComplete(experimentEntry.getExperiment().getNumCPUs(), seed,
					new TimeOut(feTimeout + amlTimeout, TimeUnit.SECONDS), new TimeOut(evalTimeout, TimeUnit.SECONDS),
					maxPipelineSize);

		} else {
			LOGGER.info("Execute AutoFEML as a two-phase process...");
			autofeml = new AutoFEMLTwoPhase(experimentEntry.getExperiment().getNumCPUs(), experiment.get("algorithm"),
					subsampleRatio, minInstances, seed, new TimeOut(feTimeout, TimeUnit.SECONDS),
					new TimeOut(amlTimeout, TimeUnit.SECONDS), new TimeOut(evalTimeout, TimeUnit.SECONDS),
					maxPipelineSize);
		}
		autofeml.setSQLAdapter(adapter, experimentEntry.getId(), CONFIG.evalTable());
		autofeml.enableVisualization(CONFIG.enableVisualization());

		LOGGER.info("Build AutoFEML classifier...");
		autofeml.buildClassifier(trainTestSplit.get(0));

		LOGGER.info("Transform test data...");
		Instances transformedTestData = autofeml.transformData(trainTestSplit.get(1));

		LOGGER.info("Evaluate selected model...");
		double errorCounter = 0.0;
		for (Instance i : transformedTestData) {
			if (i.classValue() != autofeml.classifyInstance(i)) {
				errorCounter += 1.0;
			}
		}
		double errorRate = errorCounter / transformedTestData.size();

		LOGGER.info("Selected {}" + " with a test loss of {}.", autofeml.getSelectedPipeline(), errorRate);
		Map<String, Object> results = new HashMap<>();
		results.put("loss", errorRate);
		results.put("filterpipeline", autofeml.getSelectedPipeline().getFilterPipeline().toString());
		if (autofeml.getSelectedPipeline().getMLPipeline() instanceof MLPipeline) {
			results.put("mlpipeline", autofeml.getSelectedPipeline().getMLPipeline().toString());
		} else {
			results.put("mlpipeline", WekaUtil.getClassifierDescriptor(autofeml.getSelectedPipeline().getMLPipeline()));
		}
		processor.processResults(results);

		LOGGER.info("Evaluation of experiment with id {} finished.", experimentEntry.getId());
	}

	public static void main(final String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new AutoFEMLExperimenter());
		runner.randomlyConductExperiments(1, true);
		TimeoutTimer.getInstance().stop();
		LOGGER.info("Experiment runner is shutting down.");
	}

}
