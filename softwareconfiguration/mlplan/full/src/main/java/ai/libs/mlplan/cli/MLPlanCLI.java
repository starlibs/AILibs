package ai.libs.mlplan.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.cli.module.mlc.MLPlan4MekaMultiLabelCLIModule;
import ai.libs.mlplan.cli.module.regression.MLPlan4ScikitLearnRegressionCLIModule;
import ai.libs.mlplan.cli.module.regression.MLPlan4WEKARegressionCLIModule;
import ai.libs.mlplan.cli.module.slc.MLPlan4ScikitLearnClassificationCLIModule;
import ai.libs.mlplan.cli.module.slc.MLPlan4WekaClassificationCLIModule;
import ai.libs.mlplan.cli.report.OpenMLAutoMLBenchmarkReport;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;

public class MLPlanCLI {

	// CLI variables
	private static Logger logger = LoggerFactory.getLogger(MLPlanCLI.class);
	private static final String CLI_SYNTAX = "java -jar <mlplan.jar>";
	private static final String K_SHORT_OPT = "shortOpt";
	private static final String K_DEFAULT = "default";
	private static final String K_DESCRIPTION = "description";
	private static final String K_LONG_OPT = "longOpt";
	private static final String K_HAS_ARG = "hasArg";
	private static final String K_NUM_ARGS = "numArgs";

	private static final IMLPlanCLIConfig CONFIG = ConfigFactory.create(IMLPlanCLIConfig.class);
	private static final TimeUnit DEF_TIME_UNIT = TimeUnit.valueOf(CONFIG.getDefaultTimeUnit());
	private static final int DEF_NUM_RANDOM_COMPLETIONS = 3;

	public static final String O_HELP = "h";
	public static final String O_MODULE = "m";
	public static final String O_FIT_DATASET = "f";
	public static final String O_PREDICT_DATASET = "p";
	public static final String O_LOSS = "l";
	public static final String O_SEED = "s";
	public static final String O_SSC = "ssc";
	public static final String O_NUM_CPUS = "ncpus";
	public static final String O_TIMEOUT = "t";
	public static final String O_CANDIDATE_TIMEOUT = "tc";
	public static final String O_NODE_EVAL_TIMEOUT = "tn";
	public static final String O_POS_CLASS_INDEX = "pci";
	public static final String O_POS_CLASS_NAME = "pcn";
	public static final String O_OPENML_TASK = "openMLTask";

	public static final String O_OUT_OPENML_BENCHMARK = "ooab";

	/** OPTIONAL PARAMETERS' DEFAULT VALUES */
	// Communication options standard values
	private static final List<IMLPlanCLIModule> MODULES_TO_REGISTER = Arrays.asList(new MLPlan4WekaClassificationCLIModule(), new MLPlan4ScikitLearnClassificationCLIModule(), new MLPlan4WEKARegressionCLIModule(),
			new MLPlan4ScikitLearnRegressionCLIModule(), new MLPlan4MekaMultiLabelCLIModule());
	private static Map<String, IMLPlanCLIModule> moduleRegistry = null;
	private static Map<String, String> defaults = new HashMap<>();

	private static Map<String, IMLPlanCLIModule> getModuleRegistry() {
		if (moduleRegistry != null) {
			return moduleRegistry;
		}
		moduleRegistry = new HashMap<>();
		for (IMLPlanCLIModule module : MODULES_TO_REGISTER) {
			for (String setting : module.getSettingOptionValues()) {
				moduleRegistry.put(setting, module);
			}
		}
		return moduleRegistry;
	}

	private static boolean isFlag(final JsonNode n, final String fieldName) {
		return n.has(fieldName) && n.get(fieldName).asBoolean();
	}

	private MLPlanCLI() {
		// Intentionally left blank
	}

	private static Options generateOptions() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(ResourceUtil.readResourceFileToString("config.mlplan-cli.json"));

		final Options options = new Options();
		for (JsonNode option : root.get("options")) {
			if (!option.has(K_SHORT_OPT)) {
				throw new IllegalArgumentException("Error in the cli configuration file. " + mapper.writeValueAsString(option) + " has no shortOpt field.");
			}

			options.addOption(Option.builder(option.get(K_SHORT_OPT).asText())
					// set the long name of the option
					.longOpt(option.get(K_LONG_OPT).asText())
					// set a flag whether this option is required
					.required(isFlag(option, "required"))
					// set a flag whether the option has an argument
					.hasArg(isFlag(option, K_HAS_ARG))
					// set a flag whether the argument is optional
					.optionalArg(isFlag(option, "argOptional"))
					// set the number of args
					.numberOfArgs(option.has(K_NUM_ARGS) ? option.get(K_NUM_ARGS).asInt() : (isFlag(option, K_HAS_ARG) ? 1 : 0))
					// set the description
					.desc(getDescription(option)).build());
			if (option.has(K_DEFAULT)) {
				defaults.put(option.get(K_SHORT_OPT).asText(), option.get(K_DEFAULT).asText());
			}
		}
		return options;
	}

	private static String getDescription(final JsonNode option) {
		StringBuilder sb = new StringBuilder();
		sb.append(option.get(K_DESCRIPTION).asText());
		if (option.has(K_DEFAULT)) {
			sb.append("(Default: ").append(option.get(K_DEFAULT).asText()).append(")");
		}

		if (option.get(K_SHORT_OPT).asText().equals(O_LOSS)) {
			sb.append("\n");
			for (Entry<String, IMLPlanCLIModule> entry : getModuleRegistry().entrySet()) {
				sb.append(entry.getKey()).append(": ").append(entry.getValue().getPerformanceMeasures().stream().collect(Collectors.joining(", "))).append("\n");
			}
		}

		if (option.get(K_SHORT_OPT).asText().equals(O_MODULE)) {
			sb.append("\n").append(getModuleRegistry().keySet().stream().collect(Collectors.joining(", ")));
		}

		return sb.toString();
	}

	private static CommandLine generateCommandLine(final Options options, final String[] commandLineArguments) {
		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		} catch (ParseException parseException) {
			logger.error("ERROR: Unable to parse command-line arguments {} due to exception.", Arrays.toString(commandLineArguments), parseException);
		}

		return commandLine;
	}

	private static void printUsage(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		final PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 400, CLI_SYNTAX, options);
		pw.println("use -h or --help for more detailed information about possible options.");
		pw.flush();
	}

	private static void printHelp(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(600, CLI_SYNTAX, "ML-Plan CLI v0.0.1-alpha\n================================\n", options, "===============================\nVisit us at: https://mlplan.org");
	}

	public static String getDefault(final String key) {
		return defaults.get(key);
	}

	private static void runMLPlan(final CommandLine cl) throws Exception {
		// check whether a dataset is provided for fitting.
		if (!cl.hasOption(O_FIT_DATASET) && !cl.hasOption(O_OPENML_TASK)) {
			System.err.println("Either need a training dataset provided via " + O_FIT_DATASET + " or a task and fold of an OpenML task provided via " + O_OPENML_TASK);
			System.exit(1);
		} else if (cl.hasOption(O_FIT_DATASET) && cl.hasOption(O_OPENML_TASK)) {
			System.err.println("Cannot use both: local dataset and openml task. Only one option either " + O_FIT_DATASET + " or " + O_OPENML_TASK + " may be given.");
		}

		// Load CLI modules and identify module responsible for the requested ml-plan configuration
		Map<String, IMLPlanCLIModule> moduleRegistry = getModuleRegistry();
		String moduleName = cl.getOptionValue(O_MODULE, getDefault(O_MODULE));
		if (!moduleRegistry.containsKey(moduleName)) {
			System.err.println("There is no module registered for handling the requested mode " + moduleName);
			System.exit(1);
		}
		IMLPlanCLIModule module = moduleRegistry.get(moduleName);

		// load training data
		ILabeledDataset fitDataset;
		ILabeledDataset predictDataset = null;
		if (cl.hasOption(O_OPENML_TASK)) {
			String[] taskSpec = cl.getOptionValues(O_OPENML_TASK);
			List<ILabeledDataset<ILabeledInstance>> split = OpenMLDatasetReader.loadTaskFold(Integer.parseInt(taskSpec[0]), Integer.parseInt(taskSpec[1]));
			fitDataset = split.get(0);
			predictDataset = split.get(1);
		} else {
			fitDataset = ArffDatasetAdapter.readDataset(new File(cl.getOptionValue(O_FIT_DATASET)));
		}

		// retrieve builder from module
		AMLPlanBuilder builder = module.getMLPlanBuilderForSetting(cl, fitDataset);

		// set common configs
		builder.withNumCpus(Integer.parseInt(cl.getOptionValue(O_NUM_CPUS, getDefault(O_NUM_CPUS))));
		builder.withSeed(Integer.parseInt(cl.getOptionValue(O_SEED, getDefault(O_SEED))));

		// set timeouts
		builder.withTimeOut(new Timeout(Integer.parseInt(cl.getOptionValue(O_TIMEOUT, getDefault(O_TIMEOUT))), DEF_TIME_UNIT));
		builder.withCandidateEvaluationTimeOut(new Timeout(Integer.parseInt(cl.getOptionValue(O_CANDIDATE_TIMEOUT, getDefault(O_CANDIDATE_TIMEOUT))), DEF_TIME_UNIT));
		if (cl.hasOption(O_NODE_EVAL_TIMEOUT)) {
			builder.withNodeEvaluationTimeOut(new Timeout(Integer.parseInt(cl.getOptionValue(O_NODE_EVAL_TIMEOUT, getDefault(O_NODE_EVAL_TIMEOUT))), DEF_TIME_UNIT));
		} else {
			builder.withNodeEvaluationTimeOut(new Timeout(builder.getNodeEvaluationTimeOut().seconds() * DEF_NUM_RANDOM_COMPLETIONS, DEF_TIME_UNIT));
		}

		// finally provide the training data
		builder.withDataset(fitDataset);

		// build mlplan object
		MLPlan mlplan = builder.build();
		mlplan.setLoggerName("mlplan");

		// call ml-plan to obtain the optimal supervised learner
		logger.info("Build mlplan classifier");
		ISupervisedLearner optimizedLearner = mlplan.call();

		if (predictDataset != null || cl.hasOption(O_PREDICT_DATASET)) {
			if (cl.hasOption(O_PREDICT_DATASET)) {
				File predictDatasetFile = new File(cl.getOptionValue(O_PREDICT_DATASET));
				logger.info("Load test data file: {}", predictDatasetFile.getAbsolutePath());
				predictDataset = ArffDatasetAdapter.readDataset(predictDatasetFile);
			}

			ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(optimizedLearner, predictDataset);
			logger.info("Run report of the module: {}", module.getRunReportAsString(mlplan.getSelectedClassifier(), runReport));

			if (cl.hasOption(O_OUT_OPENML_BENCHMARK)) {
				String outputFile = cl.getOptionValue(O_OUT_OPENML_BENCHMARK, getDefault(O_OUT_OPENML_BENCHMARK));
				logger.info("Generating report conforming the OpenML AutoML Benchmark format which is then written to {}.", outputFile);
				writeFile(outputFile, new OpenMLAutoMLBenchmarkReport(runReport).toString());
			}
		}
	}

	private static void writeFile(final String fileName, final String value) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)))) {
			bw.write(value);
		} catch (IOException e) {
			logger.error("Could not write value to file {}: {}", fileName, value);
		}
	}

	public static void main(final String[] args) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("Called ML-Plan CLI with the following params: {}", Arrays.toString(args));
		}
		final Options options = generateOptions();
		if (args.length == 0) {
			printUsage(options);
		} else {
			CommandLine commandLine = generateCommandLine(options, args);
			if (commandLine != null) {
				if (commandLine.hasOption(O_HELP)) {
					printHelp(options);
				} else {
					runMLPlan(commandLine);
				}
			}
		}
	}
}
