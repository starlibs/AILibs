package de.upb.crc901.mlplan.examples.multilabel.meka;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.evaluation.multilabel.ClassifierMetricGetter;
import jaicore.ml.evaluation.multilabel.MultilabelDatasetSplitter;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;

/**
 * Experimenter for ML2PLan & AutoMLC
 * 
 * @author Helena Graf
 *
 */
public class ML2PlanAutoMLCExperimenter implements IExperimentSetEvaluator {
	private static final ML2PlanAutoMLCExperimenterConfig CONFIG = ConfigCache
			.getOrCreate(ML2PlanAutoMLCExperimenterConfig.class);

	public ML2PlanAutoMLCExperimenter() {
		// nothing to do here
	}

	@Override
	public IExperimentSetConfig getConfig() {
		return CONFIG;
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter,
			final IExperimentIntermediateResultProcessor processor) throws Exception {
		System.out.println("Experiment ID: " + experimentEntry.getId());
		System.out.println("Experiment Description: " + experimentEntry.getExperiment().getValuesOfKeyFields());

		Map<String, String> experimentDescription = experimentEntry.getExperiment().getValuesOfKeyFields();

		// Load dataset
		StringBuilder datasetPathSB = new StringBuilder();
		datasetPathSB.append(CONFIG.getDatasetFolder().getAbsolutePath());
		datasetPathSB.append(File.separator);
		datasetPathSB.append(experimentDescription.get("dataset"));
		datasetPathSB.append(".arff");
		Instances data = new Instances(new FileReader(datasetPathSB.toString()));

		// Prepare the dataset to be ready for multi-label classification
		MLUtils.prepareData(data);

		// Get train / test splits
		String splitDescription_traintest = experimentDescription.get("test_split_tech");
		String testFold = experimentDescription.get("test_fold");
		String testSeed = experimentDescription.get("seed");
		Instances train = MultilabelDatasetSplitter.getTrainSplit(data, splitDescription_traintest, testFold, testSeed);
		Instances test = MultilabelDatasetSplitter.getTestSplit(data, splitDescription_traintest, testFold, testSeed);

		// Prepare connection
		ResultsDBConnection connection = new ResultsDBConnection("intermediate_measurements", "final_measurements",
				"ordered_metric", experimentEntry.getId(), "ML2Plan", adapter);

		System.out.println("Now test...");

		// Evaluation: test
		int metricIdToOptimize = Integer.parseInt(experimentDescription.get("metric_id"));
		this.evaluateMLClassifier(train, test, connection, metricIdToOptimize);

		System.out.println("Done with evaluation. Send job result.");

		Map<String, Object> results = new HashMap<>();
		results.put("completed", true);
		processor.processResults(results);

		System.out.println("Evaluation task completed.");
	}

	private void evaluateMLClassifier(final Instances train_fold, final Instances test_fold,
			final ResultsDBConnection connection, int metricIdToOptimize) throws Exception {
		Map<Integer, String> multilabelmetricsWithIds = new HashMap<>();
		for (String metric : ClassifierMetricGetter.multiLabelMetrics) {
			multilabelmetricsWithIds.put(connection.getLatestIdForMetric(metric), metric);
		}

		// TODO Init classifier here
		// Don't forget to set the metric to optimize according to the given metricId!
		// Don't forget to register a solution uploader to ml2plan for intermediate
		// solutions!
		// MultiLabelClassifier classifier = new MekaML2PlanMekaClassifier(new
		// ML2PlanMekaBuider());
		MultiLabelClassifier classifier = null;

		System.out.println("Evaluate Classifier...");
		Result result = Evaluation.evaluateModel(classifier, train_fold, test_fold);
		System.out.println("Done evaluating Classifier.");
		System.out.println("Store results in DB...");
		HashMap<String, Double> metrics = new HashMap<>();
		ClassifierMetricGetter.multiLabelMetrics.forEach(metric -> {
			metrics.put(metric, ClassifierMetricGetter.getValueOfMultilabelClassifier(result, metric));

		});
		connection.addFinalMeasurements(metrics);
		System.out.println("Stored results in DB.");
	}

	public static void main(final String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new ML2PlanAutoMLCExperimenter());
		runner.randomlyConductExperiments(false);
	}
}
