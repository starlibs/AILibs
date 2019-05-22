package de.upb.crc901.mlplan.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.AbstractMLPlanSingleLabelBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanMekaBuilder;
import de.upb.crc901.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import jaicore.basic.TimeOut;
import jaicore.concurrent.GlobalTimer;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import jaicore.ml.core.evaluation.measure.multilabel.ExactMatchLoss;
import jaicore.ml.core.evaluation.measure.multilabel.F1MacroAverageLLoss;
import jaicore.ml.core.evaluation.measure.multilabel.HammingLoss;
import jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1AsLoss;
import jaicore.ml.core.evaluation.measure.multilabel.JaccardLoss;
import jaicore.ml.core.evaluation.measure.multilabel.RankLoss;
import jaicore.ml.core.evaluation.measure.singlelabel.MeanSquaredErrorLoss;
import jaicore.ml.core.evaluation.measure.singlelabel.PrecisionAsLoss;
import jaicore.ml.core.evaluation.measure.singlelabel.RootMeanSquaredErrorLoss;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.Result;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Enables command-line usage of ML-Plan.
 *
 * @author Helena Graf
 *
 */
@SuppressWarnings("restriction")
public class MLPlanCLI {

	// CLI variables
	private static Logger logger = LoggerFactory.getLogger("MLPlanCLI");

	// MLPlan options
	private static String trainOption = "train";
	private static String testOption = "test";
	private static String totalTimeoutOption = "timeoutTotal";
	private static String nodeEvaluationTimeoutOption = "timeoutNodeEval";
	private static String solutionEvaluationTimeoutOption = "timeoutSolutionEval";
	private static String algorithmConfigurationOption = "algorithmConfig";
	private static String searchSpaceConfigurationOption = "searchSpaceConfig";
	private static String evaluationMeasureOption = "evaluationMeasure";
	private static String numCPUsOption = "numCPUS";
	private static String randomSeedOption = "randomSeed";
	private static String multiLabelOption = "multilabel";
	private static String positiveClassIndex = "positiveClassIndex";

	// MLPlan options standard values
	private static String totalTimeout = "150";
	private static String nodeEvaluationTimeout = "60";
	private static String solutionEvaluationTimeout = "60";
	private static String numCPUS = "4";
	private static String randomSeed = "0";

	// Communication options
	private static String modelFileOption = "modelFile";
	private static String resultsFileOption = "resultsFile";
	private static String printModelOption = "printModel";
	private static String visualizeOption = "visualize";
	private static String helpOption = "help";

	// Communication options standard values
	private static String modelFile = "model.txt";
	private static String resultsFile = "results.txt";

	private MLPlanCLI() {
		// Intentionally left blank
	}

	private static Options generateOptions() {
		// MLPLan options
		final Option train = Option.builder("t").required(false).hasArg().longOpt(trainOption).desc("location of the .arff training data file").build();
		final Option test = Option.builder("T").required(false).longOpt(testOption).hasArg().desc("location of the .arff test data file").build();
		final Option totalTimeout = Option.builder("tt").longOpt(totalTimeoutOption).required(false).hasArg().desc("timeout for the complete run of mlplan in seconds").build();
		final Option nodeEvaluationTimeout = Option.builder("tne").longOpt(nodeEvaluationTimeoutOption).required(false).hasArg().desc("timeout for the evaluation of a single node in seconds").build();
		final Option solutionEvaluation = Option.builder("tse").longOpt(solutionEvaluationTimeoutOption).required(false).hasArg().desc("timeout for the evaluation of a solution in seconds").build();
		final Option algorithmConfiguration = Option.builder("ac").longOpt(algorithmConfigurationOption).required(false).hasArg().desc("configuration file for mlplan").build();
		final Option searchSpaceConfiguration = Option.builder("sc").longOpt(searchSpaceConfigurationOption).required(false).hasArg().desc("search space configuration file, or alternatively: weka, weka-tiny, sklearn, sklearn-ul, meka")
				.build();
		final Option evaluationMeasure = Option.builder("em").longOpt(evaluationMeasureOption).required(false).hasArg().desc(
				"measure for assessing solution quality, allowed values: \nsinglelabel: \nERRORRATE, MEAN_SQUARED_ERROR, PRECISION, ROOT_MEAN_SQUARED_ERROR \nmultilabel: \nAUTO_MEKA_GGP_FITNESS, AUTO_MEKA_GGP_FITNESS_LOSS, EXACT_MATCH_ACCURARY, EXACT_MATCH_LOSS, F1_MACRO_AVG_D, F1_MACRO_AVG_D_LOSS, F1_MACRO_AVG_L, F1_MACRO_AVG_L_LOSS,  HAMMING_ACCURACY, HAMMING_LOSS, JACCARD_LOSS, JACCARD_SCORE, RANK_LOSS, RANK_SCORE")
				.build();
		final Option positiveClass = Option.builder("pci").longOpt(positiveClassIndex).required(false).hasArg(true).desc("Index of the class (in the list of classes) which is to be considered as the positive class").build();
		final Option numCPUS = Option.builder("ncpus").longOpt(numCPUsOption).required(false).hasArg().desc("number of used CPUs, default: " + MLPlanCLI.numCPUS).build();
		final Option randomSeed = Option.builder("rs").longOpt(randomSeedOption).required(false).hasArg().desc("randomization seed, default: " + MLPlanCLI.randomSeed).build();
		final Option multiLabel = Option.builder("ml").longOpt(multiLabelOption).required(false).hasArg(false).desc("enable for multilabel settings").build();

		// Communication options
		final Option modelFile = Option.builder("mf").longOpt(modelFileOption).required(false).hasArg()
				.desc("serialize model to the given output file, \"off\" if no model file shall be written; turn off for search spaces that contain non-serializable models").build();
		final Option resultsFile = Option.builder("rf").longOpt(resultsFileOption).required(false).hasArg().desc("serialize model to the given output file, \"off\" if no results file shall be written").build();
		final Option visualize = Option.builder("v").longOpt(visualizeOption).required(false).hasArg(false).desc("enable visualization").build();
		final Option printModel = Option.builder("p").longOpt(printModelOption).required(false).hasArg(false).desc("whether a visual representation of the final model shall be added to the model file").build();
		final Option help = Option.builder("h").longOpt(helpOption).required(false).hasArg(false).desc("supply help").build();

		// Add options to Options
		final Options options = new Options();
		options.addOption(train);
		options.addOption(test);
		options.addOption(totalTimeout);
		options.addOption(nodeEvaluationTimeout);
		options.addOption(solutionEvaluation);
		options.addOption(algorithmConfiguration);
		options.addOption(searchSpaceConfiguration);
		options.addOption(evaluationMeasure);
		options.addOption(numCPUS);
		options.addOption(randomSeed);
		options.addOption(multiLabel);
		options.addOption(resultsFile);
		options.addOption(modelFile);
		options.addOption(visualize);
		options.addOption(printModel);
		options.addOption(help);
		options.addOption(positiveClass);
		return options;
	}

	private static CommandLine generateCommandLine(final Options options, final String[] commandLineArguments) {
		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		}

		catch (ParseException parseException) {
			logger.error("ERROR: Unable to parse command-line arguments {} due to {}", Arrays.toString(commandLineArguments), parseException);
		}

		return commandLine;
	}

	private static void printUsage(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = "mlplan";
		final PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 400, syntax, options);
		pw.println("use -h or --help for help");
		pw.flush();
	}

	private static void printHelp(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = "mlplan [options]";
		formatter.printHelp(syntax, options);
	}

	private static void runMLPlan(final CommandLine commandLine) throws Exception {

		File trainDataFile = new File(commandLine.getOptionValue(trainOption));
		logger.info("Load train data file: {}", trainDataFile.getAbsolutePath());

		Instances trainData = new Instances(new FileReader(trainDataFile));
		if (commandLine.hasOption(multiLabelOption)) {
			MLUtils.prepareData(trainData);
		} else {
			trainData.setClassIndex(trainData.numAttributes() - 1);
		}

		AbstractMLPlanBuilder builder;
		if (commandLine.hasOption(searchSpaceConfigurationOption)) {
			switch (commandLine.getOptionValue(searchSpaceConfigurationOption)) {
			case "weka":
				builder = AbstractMLPlanBuilder.forWeka();
				break;
			case "weka-tiny":
				builder = AbstractMLPlanBuilder.forWeka().withTinyWekaSearchSpace();
				break;
			case "sklearn":
				builder = AbstractMLPlanBuilder.forSKLearn();
				break;
			case "sklearn-ul":
				builder = AbstractMLPlanBuilder.forSKLearn().withUnlimitedLengthPipelineSearchSpace();
				break;
			case "meka":
				builder = AbstractMLPlanBuilder.forMeka();
				break;
			default:
				throw new IllegalArgumentException("Could not identify search space configuration");
			}
		} else {
			builder = AbstractMLPlanBuilder.forWeka();
		}

		if (commandLine.hasOption(multiLabelOption)) {
			MLPlanMekaBuilder mekaBuilder = (MLPlanMekaBuilder) builder;
			switch (commandLine.getOptionValue(evaluationMeasureOption)) {
			case "AUTO_MEKA_GGP_FITNESS":
				mekaBuilder.withPerformanceMeasure(new AutoMEKAGGPFitnessMeasureLoss());
				break;
			case "EXACT_MATCH":
				mekaBuilder.withPerformanceMeasure(new ExactMatchLoss());
				break;
			case "F1_MACRO_AVG_D":
				mekaBuilder.withPerformanceMeasure(new InstanceWiseF1AsLoss());
				break;
			case "F1_MACRO_AVG_L":
				mekaBuilder.withPerformanceMeasure(new F1MacroAverageLLoss());
				break;
			case "HAMMING":
				mekaBuilder.withPerformanceMeasure(new HammingLoss());
				break;
			case "JACCARD":
				mekaBuilder.withPerformanceMeasure(new JaccardLoss());
				break;
			case "RANK_LOSS":
				mekaBuilder.withPerformanceMeasure(new RankLoss());
				break;
			default:
				throw new IllegalArgumentException("Invalid multilabel measure " + commandLine.getOptionValue(evaluationMeasureOption));
			}
		} else {
			AbstractMLPlanSingleLabelBuilder slcBuilder = (AbstractMLPlanSingleLabelBuilder) builder;

			switch (commandLine.getOptionValue(evaluationMeasureOption)) {
			case "ERRORRATE":
				slcBuilder.withPerformanceMeasure(new ZeroOneLoss());
				break;
			case "MEAN_SQUARED_ERROR":
				slcBuilder.withPerformanceMeasure(new MeanSquaredErrorLoss());
				break;
			case "PRECISION":
				int classIndex = Integer.parseInt(commandLine.getOptionValue(positiveClassIndex, "0"));
				slcBuilder.withPerformanceMeasure(new PrecisionAsLoss(classIndex));
				break;
			case "ROOT_MEAN_SQUARED_ERROR":
				slcBuilder.withPerformanceMeasure(new RootMeanSquaredErrorLoss());
				break;
			default:
				throw new IllegalArgumentException("Invalid singlelabel measure " + commandLine.getOptionValue(evaluationMeasureOption));
			}
		}

		if (commandLine.hasOption(algorithmConfigurationOption)) {
			File algoConfigFile = new File(commandLine.getOptionValue(algorithmConfigurationOption));
			builder.withAlgorithmConfigFile(algoConfigFile);
		}
		builder.withNodeEvaluationTimeOut(new TimeOut(Integer.parseInt(commandLine.getOptionValue(nodeEvaluationTimeoutOption, nodeEvaluationTimeout)), TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(Integer.parseInt(commandLine.getOptionValue(solutionEvaluationTimeoutOption, solutionEvaluationTimeout)), TimeUnit.SECONDS));
		builder.withTimeOut(new TimeOut(Integer.parseInt(commandLine.getOptionValue(totalTimeoutOption, totalTimeout)), TimeUnit.SECONDS));
		builder.withNumCpus(Integer.parseInt(commandLine.getOptionValue(numCPUsOption, numCPUS)));

		MLPlan mlplan = builder.build(trainData);
		mlplan.setLoggerName("mlplan");
		mlplan.setRandomSeed(Integer.parseInt(commandLine.getOptionValue(randomSeedOption, randomSeed)));

		Instances testData = null;
		if (commandLine.hasOption(testOption)) {
			File testDataFile = new File(commandLine.getOptionValue(testOption));
			logger.info("Load test data file: {}", testDataFile.getAbsolutePath());
			testData = new Instances(new FileReader(testDataFile));
			if (commandLine.hasOption(multiLabelOption)) {
				MLUtils.prepareData(testData);
			} else {
				testData.setClassIndex(testData.numAttributes() - 1);
			}
		}

		if (commandLine.hasOption(visualizeOption)) {
			new JFXPanel();
			AlgorithmVisualizationWindow window;
			if (commandLine.hasOption(testOption)) {
				window = new AlgorithmVisualizationWindow(mlplan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin<>(),
						new SolutionPerformanceTimelinePlugin(), new HASCOModelStatisticsPlugin(), new OutOfSampleErrorPlotPlugin(trainData, testData));
			} else {
				window = new AlgorithmVisualizationWindow(mlplan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin<>(),
						new SolutionPerformanceTimelinePlugin(), new HASCOModelStatisticsPlugin());
			}
			Platform.runLater(window);
		}

		logger.info("Build mlplan classifier");
		Classifier optimizedClassifier = mlplan.call();

		logger.info("Open timeout tasks: {}", GlobalTimer.getInstance().getActiveTasks());

		if (!"off".equals(commandLine.getOptionValue(modelFileOption))) {
			serializeModel(commandLine, mlplan.getSelectedClassifier());
		}

		if (commandLine.hasOption(testOption)) {
			double error = -1;

			if (commandLine.hasOption(multiLabelOption)) {
				logger.info("Assess test performance...");
				Result result = meka.classifiers.multilabel.Evaluation.evaluateModel((MultiLabelClassifier) mlplan.getSelectedClassifier(), trainData, testData);

				switch (commandLine.getOptionValue(evaluationMeasureOption, "AUTO_MEKA_GGP_FITNESS_LOSS")) {
				case "AUTO_MEKA_GGP_FITNESS":
					error = (Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5)) + (1 - Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5)))
							+ Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5)) + (1 - Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions()))) / 4.0;
					break;
				case "AUTO_MEKA_GGP_FITNESS_LOSS":
					error = 1 - (Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5)) + (1 - Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5)))
							+ Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5)) + (1 - Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions()))) / 4.0;
					break;
				case "EXACT_MATCH_ACCURARY":
					error = Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "EXACT_MATCH_LOSS":
					error = 1 - Metrics.P_ExactMatch(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "F1_MACRO_AVG_D":
					error = Metrics.P_FmacroAvgD(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "F1_MACRO_AVG_D_LOSS":
					error = 1 - Metrics.P_FmacroAvgD(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "F1_MACRO_AVG_L":
					error = Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "F1_MACRO_AVG_L_LOSS":
					error = 1 - Metrics.P_FmacroAvgL(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "HAMMING_ACCURACY":
					error = Metrics.P_Hamming(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "HAMMING_LOSS":
					error = Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "JACCARD_LOSS":
					error = Metrics.L_JaccardDist(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "JACCARD_SCORE":
					error = Metrics.P_JaccardIndex(result.allTrueValues(), result.allPredictions(0.5));
					break;
				case "RANK_LOSS":
					error = Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
					break;
				case "RANK_SCORE":
					error = 1 - Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
					break;
				default:
					throw new IllegalArgumentException("Invalid multilabel measure " + commandLine.getOptionValue(evaluationMeasureOption));
				}

				if (!"off".equals(commandLine.getOptionValue(resultsFileOption))) {
					writeMultiLabelEvaluationFile(result, mlplan.getInternalValidationErrorOfSelectedClassifier(), commandLine, mlplan.getSelectedClassifier());
				}
			} else {
				Evaluation eval = new Evaluation(trainData);
				logger.info("Assess test performance...");
				eval.evaluateModel(optimizedClassifier, testData);

				switch (commandLine.getOptionValue(evaluationMeasureOption, "ERRORRATE")) {
				case "ERRORRATE":
					error = eval.errorRate();
					break;
				case "MEAN_SQUARED_ERROR":
					error = Math.pow(eval.rootMeanSquaredError(), 2);
					break;
				case "ROOT_MEAN_SQUARED_ERROR":
					error = eval.rootMeanSquaredError();
					break;
				case "PRECISION":
					error = 1 - eval.precision(Integer.parseInt(commandLine.getOptionValue(positiveClassIndex, "0")));
					break;
				default:
					throw new IllegalArgumentException("Invalid singlelabel measure " + commandLine.getOptionValue(evaluationMeasureOption));
				}

				if (!"off".equals(commandLine.getOptionValue(resultsFileOption))) {
					writeSingleLabelEvaluationFile(eval, mlplan.getInternalValidationErrorOfSelectedClassifier(), commandLine, mlplan.getSelectedClassifier());
				}
			}

			logger.info("Test error was {}. Internally estimated error for this model was {}", error, mlplan.getInternalValidationErrorOfSelectedClassifier());
		}

		logger.info("Experiment done.");
	}

	private static void serializeModel(final CommandLine commandLine, final Classifier bestClassifier) throws Exception {
		SerializationHelper.write(commandLine.getOptionValue(modelFileOption, modelFile), bestClassifier);
	}

	private static void writeMultiLabelEvaluationFile(final Result result, final double internalError, final CommandLine commandLine, final Classifier bestModel) {
		StringBuilder builder = new StringBuilder();
		builder.append("Internally believed error: ");
		builder.append(internalError);
		builder.append(System.lineSeparator());
		builder.append(System.lineSeparator());
		builder.append("Best Model: ");
		builder.append(System.lineSeparator());
		builder.append(bestModel.toString());
		builder.append(System.lineSeparator());
		builder.append(System.lineSeparator());
		builder.append(result.toString());
		builder.append(System.lineSeparator());
		builder.append(System.lineSeparator());
		if (commandLine.hasOption(printModelOption)) {
			builder.append("Classifier Representation: ");
			builder.append(System.lineSeparator());
			builder.append(System.lineSeparator());
			if (bestModel instanceof MLPipeline) {
				builder.append(((MLPipeline) bestModel).getBaseClassifier().toString());
			} else {
				builder.append(bestModel.toString());
			}
		}

		writeFile(commandLine.getOptionValue(resultsFileOption, resultsFile), builder.toString());
	}

	private static void writeSingleLabelEvaluationFile(final Evaluation eval, final double internalError, final CommandLine commandLine, final Classifier bestModel) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("Internally believed error: ");
		builder.append(internalError);
		builder.append(System.lineSeparator());
		builder.append(System.lineSeparator());
		builder.append("Best Model: ");
		builder.append(System.lineSeparator());
		builder.append(bestModel.toString());
		builder.append(System.lineSeparator());
		builder.append(System.lineSeparator());
		builder.append(eval.toSummaryString("Summary", true));
		builder.append(System.lineSeparator());
		builder.append(eval.toClassDetailsString("Class Details"));
		builder.append(System.lineSeparator());
		builder.append("Evaluation Overview");
		builder.append(System.lineSeparator());
		builder.append(eval.toCumulativeMarginDistributionString());
		builder.append(System.lineSeparator());
		builder.append(eval.toMatrixString("Matrix"));
		if (commandLine.hasOption(printModelOption)) {
			builder.append("Classifier Representation: ");
			builder.append(System.lineSeparator());
			builder.append(System.lineSeparator());
			if (bestModel instanceof MLPipeline) {
				builder.append(((MLPipeline) bestModel).getBaseClassifier().toString());
			} else {
				builder.append(bestModel.toString());
			}
		}

		writeFile(commandLine.getOptionValue(resultsFileOption, resultsFile), builder.toString());
	}

	private static void writeFile(final String fileName, final String value) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)))) {
			bw.write(value);
		} catch (IOException e) {
			logger.error("Could not write value to file {}: {}", fileName, value);
		}
	}

	public static void main(final String[] args) throws Exception {
		final Options options = generateOptions();
		if (args.length == 0) {
			printUsage(options);
		} else {
			CommandLine commandLine = generateCommandLine(options, args);
			if (commandLine != null) {
				if (commandLine.hasOption(helpOption)) {
					printHelp(options);
				} else {
					runMLPlan(commandLine);
				}
			}
		}
	}
}
