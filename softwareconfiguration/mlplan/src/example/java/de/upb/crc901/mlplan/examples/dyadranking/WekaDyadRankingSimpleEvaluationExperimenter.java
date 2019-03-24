package de.upb.crc901.mlplan.examples.dyadranking;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.openml.apiconnector.settings.Settings;

import de.upb.crc901.mlpipeline_evaluation.SimpleResultsUploader;
import de.upb.crc901.mlpipeline_evaluation.SimpleUploaderMeasureBridge;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.metamining.dyadranking.WEKADyadRankedNodeQueueConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory;
import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.dyadranking.search.RandomlyRankedNodeQueueConfig;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class WekaDyadRankingSimpleEvaluationExperimenter implements IExperimentSetEvaluator {

	private static final File configFile = new File("conf/dyadranking/dyadmlplan.properties");
	private WekaDyadRankingSimpleEvaluationConfig config;
	private static int arrayJobNr;

	public WekaDyadRankingSimpleEvaluationExperimenter(File configFile) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configFile));
		} catch (IOException e) {
			System.err.println("Could not find or access config file " + configFile);
			System.exit(1);
		}
		this.config = ConfigFactory.create(WekaDyadRankingSimpleEvaluationConfig.class, props);
	}

	@Override
	public IExperimentSetConfig getConfig() {
		return config;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
			IExperimentIntermediateResultProcessor processor) throws Exception {
		// Put ArrayjobNr in fields to identify error logs from db table
		Map<String, Object> results = new HashMap<>();
		results.put("arrayjob_nr", arrayJobNr);
		processor.processResults(results);
		results.clear();

		// Remember start time
		long startTimeTotal = System.currentTimeMillis();

		Map<String, String> keyfields = experimentEntry.getExperiment().getValuesOfKeyFields();
		System.out.println("Start experiment with keys: " + keyfields);

		// Adapt config to current experiment
		this.config.setProperty("selection_data_portion",
				experimentEntry.getExperiment().getValuesOfKeyFields().get("selection_data_portion"));
		this.config.setProperty("mlplan.selection.data_portion",
				experimentEntry.getExperiment().getValuesOfKeyFields().get("selection_data_portion"));

		// Get and split data
		Settings.CACHE_ALLOWED = false;
		ReproducibleInstances data = ReproducibleInstances.fromOpenML(keyfields.get("dataset"),
				"4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances) data,
				(new Random(Integer.parseInt(keyfields.get("seed")))).nextLong(), 0.7d);

		// Builder for normal ML-Plan
		MLPlanBuilder builder = new MLPlanBuilder()
				.withSearchSpaceConfigFile(new File("conf/automl/searchmodels/weka/weka-approach-5-autoweka.json"))
				.withAlgorithmConfigFile(new File("conf/automl/mlplan.properties"))
				.withPerformanceMeasure(MultiClassPerformanceMeasure.ERRORRATE)
				.withCustomEvaluatorBridge(new SimpleUploaderMeasureBridge(new SimpleResultsUploader(adapter,
						config.evaluationsTable(), keyfields.get("algorithm"), experimentEntry.getId())));

		// Configure special cases (Random, Dyad ML-Plan)
		WEKADyadRankedNodeQueueConfig openConfig = new WEKADyadRankedNodeQueueConfig();
		if (keyfields.get("algorithm").equals("dyad_mlplan")) {
			builder.setHascoFactory(new HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(openConfig));
			builder.prepareNodeEvaluatorInFactoryWithData(data);
		} else if (keyfields.get("algorithm").equals("random")) {
			builder.setHascoFactory(new HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(
					new RandomlyRankedNodeQueueConfig<TFDNode>(Integer.parseInt(keyfields.get("seed")))));
			builder.prepareNodeEvaluatorInFactoryWithData(data);
		}

		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);

		// Characterize dataset (if needed) and inform db of how long it took
		long datasettime = System.currentTimeMillis();
		if (keyfields.get("algorithm").equals("dyad_mlplan")) {
			openConfig.setComponents(mlplan.getComponents());
			openConfig.setData(split.get(0));
		}
		datasettime = System.currentTimeMillis() - datasettime;
		results.put("datasetevaltime", datasettime);
		processor.processResults(results);
		results.clear();

		long trainTime = 0;
		double result = 1;
		try {
			// Start ML-Plan with the correct timeout
			long startTimeTraining = System.currentTimeMillis();
			mlplan.setTimeout(new TimeOut(
					Integer.parseInt(keyfields.get("timeout")) * 60 - (startTimeTraining - startTimeTotal) / 1000,
					TimeUnit.SECONDS));
			mlplan.buildClassifier(split.get(0));
			trainTime = (int) (System.currentTimeMillis() - startTimeTraining) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");
			results.put("mlplanevaltime", trainTime);
			results.put("classifier",
					SimpleResultsUploader.getSolutionString((MLPipeline) mlplan.getSelectedClassifier()));
			processor.processResults(results);
			results.clear();

			// Evaluated produced solution
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(mlplan, split.get(1));
			result = (100 - eval.pctCorrect()) / 100f;
			System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
			System.out.println("Total experiment time: " + (System.currentTimeMillis() - startTimeTotal) / 1000);
			return;
		}
		System.out.println("Total experiment time: " + (System.currentTimeMillis() - startTimeTotal) / 1000);

		results.put("loss", result);
		processor.processResults(results);
	}

	private static void print(final String message) {
		System.out.println(new Time(System.currentTimeMillis()).toString() + ": " + message);
	}

	public static void main(final String[] args) {
		WekaDyadRankingSimpleEvaluationExperimenter.arrayJobNr = Integer.parseInt(args[0]);

		/* check config */
		print("Start experiment runner...");
		ExperimentRunner runner = new ExperimentRunner(new WekaDyadRankingSimpleEvaluationExperimenter(configFile));
		print("Conduct random experiment...");
		runner.randomlyConductExperiments(false);
		print("Experiment conducted, stop timeout timer.");
		TimeoutTimer.getInstance().stop();
		print("Timer stopped.");
	}

}
