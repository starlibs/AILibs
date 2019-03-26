package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLPlanWekaCLI {

	private static final Logger L = LoggerFactory.getLogger(MLPlanWekaCLI.class);
	private static final File PROPERTIES_FILE = new File("conf/mlplanwekacli.properties");

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		if (args.length > 0 && args[0].equals("-h")) {
			L.info("Parameters to set: ");
			L.info("<dataset_file> <global_timeout> <evaluation_timeout>");
			System.exit(0);
		}

		Properties properties = new Properties();
		properties.load(new FileInputStream(PROPERTIES_FILE));
		final MLPlanWekaCLIConfig cliConfig = ConfigFactory.create(MLPlanWekaCLIConfig.class, properties);
		L.info("Config {} initialized.", cliConfig);

		/* set dataset file if given */
		if (args.length > 0) {
			cliConfig.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_DATASET_FILE, args[0]);
		}
		/* set global timeout, if given */
		if (args.length > 1) {
			cliConfig.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_TIMEOUT, args[1]);
		}
		/* set timeout for single evaluation, if given */
		if (args.length > 2) {
			cliConfig.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_EVAL_TIMEOUT, args[2]);
		}

		/* set ports for pipeline plans */
		if (L.isInfoEnabled()) {
			L.info("Load dataset {}...", cliConfig.datasetFile());
		}
		Instances data = null;
		try {
			data = new Instances(new FileReader(cliConfig.datasetFile()));
		} catch (IOException e) {
			L.error("Could not load dataset at {}", cliConfig.datasetFile());
			System.exit(1);
		}
		data.setClassIndex(data.numAttributes() - 1);
		L.info("Dataset loaded.");

		/* extract all relevant information about the experiment */
		L.info("Initialize ML-Plan...");
		MLPlanBuilder builder = new MLPlanBuilder();
		builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(cliConfig.evalTimeout(), TimeUnit.SECONDS));

		MLPlanWekaClassifier mlPlan = new WekaMLPlanWekaClassifier(builder);
		mlPlan.setTimeout(new TimeOut(cliConfig.timeout(), TimeUnit.SECONDS));
		mlPlan.setVisualizationEnabled(cliConfig.showGraphVisualization());

		L.info("Split the data into train and test set...");
		List<Instances> testSplit = WekaUtil.getStratifiedSplit(data, mlPlan.getMLPlanConfig().randomSeed(), 0.7);
		L.info("Data split created.");

		try {
			L.info("Run ML-Plan...");
			mlPlan.buildClassifier(testSplit.get(0));
			L.info("Execution of ML-Plan finished.");

			L.info("Best Solution found: {}", mlPlan.getSelectedClassifier());

			L.info("Assess quality of found ");
			Evaluation eval = new Evaluation(data);
			eval.evaluateModel(mlPlan, testSplit.get(1), new Object[] {});

			StringBuilder sb = new StringBuilder();
			sb.append("Pct Correct: " + eval.pctCorrect());
			sb.append("\n");
			sb.append("Pct Incorrect: " + eval.pctIncorrect());
			sb.append("\n");
			sb.append("Pct Unclassified: " + eval.pctUnclassified());
			sb.append("\n");
			sb.append("True Positive Rate: " + eval.truePositiveRate(0));
			sb.append("\n");
			sb.append("False Positive Rate: " + eval.falsePositiveRate(0));
			sb.append("\n");
			sb.append("False Negative Rate: " + eval.falseNegativeRate(0));
			sb.append("\n");
			sb.append("True Negative Rate: " + eval.trueNegativeRate(0));
			sb.append("\n");
			sb.append("F-Measure: " + eval.fMeasure(0));
			sb.append("\n");
			sb.append("Weighted F-Measure: " + eval.weightedFMeasure());
			sb.append("\n");
			sb.append("Area under ROC: " + eval.areaUnderROC(0));
			sb.append("\n");
			sb.append("Area under PRC: " + eval.areaUnderPRC(0));
			sb.append("\n");
			sb.append("Error Rate: " + eval.errorRate());
			sb.append("\n");
			sb.append("Unclassified: " + eval.unclassified());
			sb.append("\n");
			sb.append("Incorrect: " + eval.incorrect());
			sb.append("\n");
			sb.append("Correct: " + eval.correct());
			sb.append("\n");
			sb.append("Kappa: " + eval.kappa());
			sb.append("\n");
			sb.append("Root Mean Squared Error: " + eval.rootMeanSquaredError());
			sb.append("\n");
			sb.append("Mean Absolute Error: " + eval.meanAbsoluteError());
			sb.append("\n");
			sb.append("Precision: " + eval.precision(0));
			sb.append("\n");
			sb.append("Recall: " + eval.recall(0));
			sb.append("\n");

			L.info("{}", sb);

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(cliConfig.outputFile()))) {
				bw.write("Best Solution found: " + mlPlan.getSelectedClassifier());
				bw.write("\n");
				bw.write("============================================");
				bw.write("\n");
				bw.write("Measured test performance: ");
				bw.write("\n");
				bw.write(sb.toString());
			}

			L.info("Accuracy: {}", eval.pctCorrect() / 100);
			L.info("Error Rate: {}", eval.errorRate());
			L.info("Unweighted Macro F Measure: {}", eval.unweightedMacroFmeasure());
			L.info("Weighted F Measure: {}", eval.weightedFMeasure());
		} catch (Exception e) {
			L.error("Could not successfully execute ML-Plan.", e);
		}
	}
}
