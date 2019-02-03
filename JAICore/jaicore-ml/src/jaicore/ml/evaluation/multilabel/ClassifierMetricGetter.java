package jaicore.ml.evaluation.multilabel;

import java.util.Arrays;
import java.util.List;

import meka.core.Metrics;
import meka.core.Result;
import weka.classifiers.Evaluation;

/**
 * Class for getting metrics by their name for single- and multilabel
 * classifiers.
 * 
 * @author Helena Graf
 *
 */
public class ClassifierMetricGetter {

	/**
	 * Available metric for singlelabelclassifiers
	 */
	public static final List<String> singleLabelMetrics = Arrays.asList("correct", "incorrect", "kappa", "totalCost",
			"avgCost", "KBRelativeInformation", "KBMeanInformation", "KBInformation", "correlationCoefficient",
			"rootMeanSquaredError", "rootMeanPriorSquaredError", "coverageOfTestCasesByPredictedRegions",
			"sizeOfPredictedRegions", "rootRelativeSquaredError", "truePositiveRate", "numTruePositives",
			"weightedTruePositiveRate", "trueNegativeRate", "numTrueNegatives", "weightedTrueNegativeRate",
			"falsePositiveRate", "numFalsePositives", "weightedFalsePositiveRate", "falseNegativeRate",
			"numFalseNegatives", "weightedFalseNegativeRate", "precision", "weightedPrecision", "recall",
			"weightedRecall", "fMeasure", "weightedFMeasure", "unweightedMacroFmeasure", "unweightedMicroFmeasure",
			"matthewsCorrelationCoefficient", "weightedMatthewsCorrelation", "areaUnderROC", "weightedAreaUnderROC",
			"areaUnderPRC", "weightedAreaUnderPRC", "errorRate", "meanAbsoluteError", "meanPriorAbsoluteError",
			"relativeAbsoluteError", "numInstances", "pctCorrect", "pctIncorrect", "pctUnclassified", "priorEntropy");

	/**
	 * Available metrics for multilabelclassifiers
	 */
	public static final List<String> multiLabelMetrics = Arrays.asList("L_Hamming", "L_LevenshteinDistance",
			"L_LogLoss", "L_LogLossD", "L_LogLossL", "L_OneError", "L_JaccardDist", "L_RankLoss", "L_ZeroOne",
			"P_Accuracy", "P_AveragePrecision", "P_ExactMatch", "P_FmacroAvgD", "P_FmacroAvgL", "P_FmicroAvg", "P_Hamming",
			"P_Harmonic", "P_JaccardIndex", "P_macroAUPRC", "P_marcoAUROC", "P_RecallMicro", "P_RecallMacro",
			"P_PrecisionMicro", "P_PrecisionMacro", "P_Fitness");

	/**
	 * Extracts the metric with the given name from the Evaluation object that is
	 * the result of evaluating a classifier.
	 * 
	 * @param eval
	 *            The Evaluation object containing the results of evaluating a
	 *            classifier
	 * @param metricName
	 *            The name of the metric to retrieve
	 * @param classIndex
	 *            The class index in the instances the classifier was evaluated on
	 * @return The value of the metric
	 * @throws Exception
	 *             If a metric cannot be retrieved
	 */
	public static double getValueOfMetricForSingleLabelClassifier(Evaluation eval, String metricName, int classIndex)
			throws Exception {
		switch (metricName) {
		case "correct":
			return eval.correct();
		case "incorrect":
			return eval.incorrect();
		case "kappa":
			return eval.kappa();
		case "totalCost":
			return eval.totalCost();
		case "avgCost":
			return eval.avgCost();
		case "KBRelativeInformation":
			return eval.KBRelativeInformation();
		case "KBMeanInformation":
			return eval.KBMeanInformation();
		case "KBInformation":
			return eval.KBInformation();
		case "correlationCoefficient":
			return eval.correlationCoefficient();
		case "rootMeanSquaredError":
			return eval.rootMeanSquaredError();
		case "rootMeanPriorSquaredError":
			return eval.rootMeanPriorSquaredError();
		case "coverageOfTestCasesByPredictedRegions":
			return eval.coverageOfTestCasesByPredictedRegions();
		case "sizeOfPredictedRegions":
			return eval.sizeOfPredictedRegions();
		case "rootRelativeSquaredError":
			return eval.rootRelativeSquaredError();
		case "truePositiveRate":
			return eval.truePositiveRate(classIndex);
		case "numTruePositives":
			return eval.numTruePositives(classIndex);
		case "weightedTruePositiveRate":
			return eval.weightedTruePositiveRate();
		case "trueNegativeRate":
			return eval.trueNegativeRate(classIndex);
		case "numTrueNegatives":
			return eval.numTrueNegatives(classIndex);
		case "weightedTrueNegativeRate":
			return eval.weightedTrueNegativeRate();
		case "falsePositiveRate":
			return eval.falsePositiveRate(classIndex);
		case "numFalsePositives":
			return eval.numFalsePositives(classIndex);
		case "weightedFalsePositiveRate":
			return eval.weightedFalsePositiveRate();
		case "falseNegativeRate":
			return eval.falseNegativeRate(classIndex);
		case "numFalseNegatives":
			return eval.numFalseNegatives(classIndex);
		case "weightedFalseNegativeRate":
			return eval.weightedFalseNegativeRate();
		case "precision":
			return eval.precision(classIndex);
		case "weightedPrecision":
			return eval.weightedPrecision();
		case "recall":
			return eval.recall(classIndex);
		case "weightedRecall":
			return eval.weightedRecall();
		case "fMeasure":
			return eval.fMeasure(classIndex);
		case "weightedFMeasure":
			return eval.weightedFMeasure();
		case "unweightedMacroFmeasure":
			return eval.unweightedMacroFmeasure();
		case "unweightedMicroFmeasure":
			return eval.unweightedMicroFmeasure();
		case "matthewsCorrelationCoefficient":
			return eval.matthewsCorrelationCoefficient(classIndex);
		case "weightedMatthewsCorrelation":
			return eval.weightedMatthewsCorrelation();
		case "areaUnderROC":
			return eval.areaUnderROC(classIndex);
		case "weightedAreaUnderROC":
			return eval.weightedAreaUnderROC();
		case "areaUnderPRC":
			return eval.areaUnderPRC(classIndex);
		case "weightedAreaUnderPRC":
			return eval.weightedAreaUnderPRC();
		case "errorRate":
			return eval.errorRate();
		case "meanAbsoluteError":
			return eval.meanAbsoluteError();
		case "meanPriorAbsoluteError":
			return eval.meanPriorAbsoluteError();
		case "relativeAbsoluteError":
			return eval.relativeAbsoluteError();
		case "numInstances":
			return eval.numInstances();
		case "pctCorrect":
			return eval.pctCorrect();
		case "pctIncorrect":
			return eval.pctIncorrect();
		case "pctUnclassified":
			return eval.pctUnclassified();
		case "priorEntropy":
			return eval.priorEntropy();
		}

		throw new IllegalArgumentException(metricName + " not a supported metric!");
	}

	/**
	 * Extracts the metric with the given name from the result of evaluating a
	 * multilabel classifier (Calls the corresponding method).
	 * 
	 * @param result
	 *            The result of evaluating the classifier
	 * @param metricName
	 *            The metric which should be retrieved
	 * @return The value of the metric
	 */
	public static double getValueOfMultilabelClassifier(Result result, String metricName) {

		switch (metricName) {
		case "L_Hamming":
			return Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5));
		case "L_LevenshteinDistance":
			return Metrics.L_LevenshteinDistance(result.allTrueValues(), result.allPredictions(0.5));
		case "L_LogLoss":
			return Metrics.L_LogLoss(result.allTrueValues(), result.allPredictions(), 0.5);
		case "L_LogLossD":
			return Metrics.L_LogLossD(result.allTrueValues(), result.allPredictions());
		case "L_LogLossL":
			return Metrics.L_LogLossL(result.allTrueValues(), result.allPredictions());
		case "L_OneError":
			return Metrics.L_OneError(result.allTrueValues(), result.allPredictions());
		case "L_JaccardDist":
			return Metrics.L_JaccardDist(result.allTrueValues(), result.allPredictions(0.5));
		case "L_RankLoss":
			return Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
		case "L_ZeroOne":
			return Metrics.L_ZeroOne(result.allTrueValues(), result.allPredictions(0.5));
		case "P_Accuracy":
			return Metrics.P_Accuracy(result.allTrueValues(), result.allPredictions(0.5));
		case "P_AveragePrecision":
			return Metrics.P_AveragePrecision(result.allTrueValues(), result.allPredictions());
		case "P_ExactMatch":
			return Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5));
		case "P_FmacroAvgD":
			return Metrics.P_FmacroAvgD(result.allTrueValues(), result.allPredictions(0.5));
		case "P_FmacroAvgL":
			return Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5));
		case "P_FmicroAvg":
			return Metrics.P_FmicroAvg(result.allTrueValues(), result.allPredictions(0.5));
		case "P_Hamming":
			return Metrics.P_Hamming(result.allTrueValues(), result.allPredictions(0.5));
		case "P_Harmonic":
			return Metrics.P_Harmonic(result.allTrueValues(), result.allPredictions(0.5));
		case "P_JaccardIndex":
			return Metrics.P_JaccardIndex(result.allTrueValues(), result.allPredictions(0.5));
		case "P_macroAUPRC":
			return Metrics.P_macroAUPRC(result.allTrueValues(), result.allPredictions());
		case "P_marcoAUROC":
			return Metrics.P_macroAUROC(result.allTrueValues(), result.allPredictions());
		case "P_RecallMicro":
			return Metrics.P_RecallMicro(result.allTrueValues(), result.allPredictions(0.5));
		case "P_RecallMacro":
			return Metrics.P_RecallMacro(result.allTrueValues(), result.allPredictions(0.5));
		case "P_PrecisionMicro":
			return Metrics.P_PrecisionMicro(result.allTrueValues(), result.allPredictions(0.5));
		case "P_PrecisionMacro":
			return Metrics.P_PrecisionMacro(result.allTrueValues(), result.allPredictions(0.5));
		case "P_Fitness":
			return (Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5))
					+ (1 - Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5)))
					+ Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5))
					+ (1 - Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions()))) / 4.0;
		}

		throw new IllegalArgumentException(metricName + " not a supported metric!");
	}
}
