package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLPlanWekaCLI {

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		
		if (args.length > 0 && args[0].equals("-h")) {
			System.out.println("Parameters to set: ");
			System.out.println("<dataset_file> <global_timeout> <evaluation_timeout>");
			System.exit(0);
		}
		
		Properties properties = new Properties();
		properties.load(new FileInputStream("conf/mlplan/mlplanwekacli.properties"));
		final MLPlanWekaCLIConfig CLI_CONFIG = ConfigFactory.create(MLPlanWekaCLIConfig.class, properties);
		System.out.println("Config " + CLI_CONFIG + " initialized.");

		/* set dataset file if given */
		if (args.length > 0) {
			CLI_CONFIG.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_DATASET_FILE, args[0]);
		}
		/* set global timeout, if given */
		if (args.length > 1) {
			CLI_CONFIG.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_TIMEOUT, args[1]);
		}
		/* set timeout for single evaluation, if given */
		if (args.length > 2) {
			CLI_CONFIG.setProperty(MLPlanWekaCLIConfig.K_MLPLAN_EVAL_TIMEOUT, args[2]);
		}

		/* set ports for pipeline plans */
		System.out.println(getTime() + " Load dataset " + CLI_CONFIG.datasetFile() + "...");
		Instances data = null;
		try {
			data = new Instances(new FileReader(CLI_CONFIG.datasetFile()));
		} catch (IOException e) {
			System.err.println("Could not load dataset at " + CLI_CONFIG.datasetFile());
			System.exit(1);
		}
		data.setClassIndex(data.numAttributes() - 1);
		System.out.println(getTime() + " Dataset loaded.");

		/* extract all relevant information about the experiment */
		System.out.println(getTime() + " Initialize ML-Plan...");
		MLPlanWekaClassifier mlPlan = new WekaMLPlanWekaClassifier();
		mlPlan.setTimeout(CLI_CONFIG.timeout());
		mlPlan.setTimeoutForSingleSolutionEvaluation(CLI_CONFIG.evalTimeout() * 1000);
		if (CLI_CONFIG.showGraphVisualization())
			mlPlan.activateVisualization();

		System.out.println(getTime() + " Split the data into train and test set...");
		List<Instances> testSplit = WekaUtil.getStratifiedSplit(data, new Random(mlPlan.getConfig().randomSeed()), 0.7);
		System.out.println("Data split created.");

		try {
			System.out.println("Run ML-Plan...");
			mlPlan.buildClassifier(testSplit.get(0));
			System.out.println("Execution of ML-Plan finished.");

			System.out.println("Best Solution found:" + mlPlan.getSelectedClassifier());

			System.out.println("Assess quality of found ");
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

			System.out.println(sb.toString());

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(CLI_CONFIG.outputFile()))) {
				bw.write("Best Solution found: " + mlPlan.getSelectedClassifier());
				bw.write("\n");
				bw.write("============================================");
				bw.write("\n");
				bw.write("Measured test performance: ");
				bw.write("\n");
				bw.write(sb.toString());
			}

			System.out.println("Accuracy: " + eval.pctCorrect() / 100);
			System.out.println("Error Rate: " + eval.errorRate());
			System.out.println("Unweighted Macro F Measure: " + eval.unweightedMacroFmeasure());
			System.out.println("Weighted F Measure: " + eval.weightedFMeasure());
		} catch (Exception e) {
			System.out.println("Could not successfully execute ML-PLan.");
			e.printStackTrace();
		}
	}

	private static String getTime() {
		return "[" + new Time(System.currentTimeMillis()).toString() + "]";
	}
}
