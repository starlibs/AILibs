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
import jaicore.ml.evaluation.multilabel.databaseconnection.ClassifierDBConnection;
import jaicore.ml.evaluation.multilabel.databaseconnection.EvaluationMode;
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
		String testSeed = experimentDescription.get("test_seed");
		String valSeed = experimentDescription.get("val_seed");
		Instances train = MultilabelDatasetSplitter.getTrainSplit(data, splitDescription_traintest, testFold, testSeed);
		Instances test = MultilabelDatasetSplitter.getTestSplit(data, splitDescription_traintest, testFold, testSeed);

		// Get validation splits
		String splitDescription_validation = experimentDescription.get("val_split_tech");
		String validation_fold = experimentDescription.get("val_fold");
		Instances validation_fold_0 = MultilabelDatasetSplitter.getTrainSplit(train, splitDescription_validation,
				validation_fold, valSeed);
		Instances validation_fold_1 = MultilabelDatasetSplitter.getTestSplit(train, splitDescription_validation,
				validation_fold, valSeed);

		// Prepare connection
		ClassifierDBConnection connection = new ClassifierDBConnection(adapter, experimentDescription.get("dataset"),
				splitDescription_traintest, testFold, splitDescription_validation, validation_fold, testSeed, valSeed);

		System.out.println("Now validate...");

		// Evaluation: validation
		connection.setMode(EvaluationMode.Validation);
		this.evaluateMLClassifier(validation_fold_0, validation_fold_1, connection);

		System.out.println("Now test...");

		// Evaluation: test
		connection.setMode(EvaluationMode.Test);
		this.evaluateMLClassifier(validation_fold_0, test, connection);

		System.out.println("Done with evaluation. Send job result.");

		Map<String, Object> results = new HashMap<>();
		results.put("completed", true);
		processor.processResults(results);

		System.out.println("Evaluation task completed.");
	}

	private void evaluateMLClassifier(final Instances train_fold, final Instances test_fold,
			final ClassifierDBConnection connection) throws Exception {
		Map<Integer, String> multilabelmetricsWithIds = new HashMap<>();
		for (String metric : ClassifierMetricGetter.multiLabelMetrics) {
			multilabelmetricsWithIds.put(connection.getLatestIdForMetric(metric), metric);
		}

		// TODO Init classifier here
		// MultiLabelClassifier classifier = new MekaML2PlanMekaClassifier(new
		// ML2PlanMekaBuider());
		MultiLabelClassifier classifier = null;

		System.out.println("Evaluate Classifier...");
		Result result = Evaluation.evaluateModel(classifier, train_fold, test_fold);
		System.out.println("Done evaluating Classifier.");
		System.out.println("Store results in DB...");
		// TODO here the classifier is m2lplan, but for automlc it will be automlc
		connection.addMeasurementsForMultilabelClassifierIfNotExists(connection.getOrCreateIdForClassifier("ml2plan"),
				result, multilabelmetricsWithIds);
		System.out.println("Stored results in DB.");
	}

	public static void main(final String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new ML2PlanAutoMLCExperimenter());
		runner.randomlyConductExperiments(false);
	}
}
