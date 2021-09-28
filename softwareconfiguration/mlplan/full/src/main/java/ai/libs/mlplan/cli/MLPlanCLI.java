package ai.libs.mlplan.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
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
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xml.DataFeature.Feature;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Task;
import org.openml.apiconnector.xml.Task.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.hasco.gui.civiewplugin.TFDNodeAsCIViewInfoGenerator;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapperConfig;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rolloutboxplots.SearchRolloutBoxplotPlugin;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.cli.module.regression.MLPlan4ScikitLearnRegressionCLIModule;
import ai.libs.mlplan.cli.module.regression.MLPlan4WEKARegressionCLIModule;
import ai.libs.mlplan.cli.module.slc.MLPlan4ScikitLearnClassificationCLIModule;
import ai.libs.mlplan.cli.module.slc.MLPlan4WekaClassificationCLIModule;
import ai.libs.mlplan.cli.report.OpenMLAutoMLBenchmarkReport;
import ai.libs.mlplan.cli.report.StatisticsListener;
import ai.libs.mlplan.cli.report.StatisticsReport;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.python.IPythonConfig;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

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

	public static final String O_HELP = "h"; // print help
	public static final String O_MODULE = "m"; // select module
	public static final String O_FIT_DATASET = "f"; // provide fit dataset
	public static final String O_PREDICT_DATASET = "p"; // provide predict dataset
	public static final String O_LOSS = "l"; // specify loss function to use
	public static final String O_SEED = "s";
	public static final String O_SSC = "ssc";
	public static final String O_NUM_CPUS = "ncpus";
	public static final String O_TIMEOUT = "t";
	public static final String O_VISUALIZATION = "v";
	public static final String O_CANDIDATE_TIMEOUT = "tc";
	public static final String O_NODE_EVAL_TIMEOUT = "tn";
	public static final String O_POS_CLASS_INDEX = "pci";
	public static final String O_POS_CLASS_NAME = "pcn";
	public static final String O_OPENML_TASK = "openMLTask"; // id of an openML taks as an alternative to fit and predict datasets

	public static final String O_OUT_OPENML_BENCHMARK = "ooab";
	public static final String O_OUT_STATS = "os";
	public static final String O_OUT_MODEL = "om";

	public static final String O_TMP = "tmp";
	public static final String O_PYTHON_CMD = "pythonCmd";

	/** OPTIONAL PARAMETERS' DEFAULT VALUES */
	// Communication options standard values
	private static final List<IMLPlanCLIModule> MODULES_TO_REGISTER = Arrays.asList(new MLPlan4WekaClassificationCLIModule(), new MLPlan4ScikitLearnClassificationCLIModule(), new MLPlan4WEKARegressionCLIModule(),
			new MLPlan4ScikitLearnRegressionCLIModule());
	private static Map<String, IMLPlanCLIModule> moduleRegistry = null;
	private static Map<String, String> defaults = new HashMap<>();
	private static String version;

	private static Double testPerformance;
	private static IComponentInstance incumbent;

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
		version = root.get("version").asText();
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
		formatter.printHelp(600, CLI_SYNTAX, "ML-Plan CLI " + version + "\n================================\n", options, "===============================\nVisit us at: https://mlplan.org");
	}

	public static String getDefault(final String key) {
		return defaults.get(key);
	}

	private static List<ILabeledDataset<ILabeledInstance>> loadOpenMLTaskAsTrainTestSplit(final int taskID, final int fold) throws Exception {

		logger.info("Load train test split of task {} and fold {}", taskID, fold);
		OpenmlConnector con = new OpenmlConnector();
		Task omlTask = con.taskGet(taskID);
		File foldAssignmentFile = con.taskSplitsGet(omlTask);

		Instances splitDescription = new Instances(new FileReader(foldAssignmentFile));
		splitDescription.setClassIndex(splitDescription.numAttributes() - 1);
		List<Integer> fitFold = new ArrayList<>();
		List<Integer> predictFold = new ArrayList<>();

		for (Instance i : splitDescription) {
			if (((int) i.classValue()) == fold) {
				int instanceIndex = (int) i.value(1);
				switch (splitDescription.attribute(0).value((int) i.value(0))) {
				case "TRAIN":
					fitFold.add(instanceIndex);
					break;
				case "TEST":
					predictFold.add(instanceIndex);
					break;
				default:
					/* ignore this case */
					break;
				}
			}
		}

		ILabeledDataset<?> dataset = null;
		for (Input input : omlTask.getInputs()) {
			if (input.getName().equals("source_data")) {
				DataSetDescription dsd = input.getData_set().getDataSetDescription(con);
				DataFeature feature = con.dataFeatures(dsd.getId());

				List<String> removeAttributes = new ArrayList<>();
				for (Entry<String, Feature> featureEntry : feature.getFeaturesAsMap().entrySet()) {
					if (featureEntry.getValue().getIs_row_identifier() || featureEntry.getValue().getIs_ignore()) {
						removeAttributes.add(featureEntry.getKey());
					}
				}

				Instances wekaData = new Instances(new FileReader(con.datasetGet(con.dataGet(input.getData_set().getData_set_id()))));
				String rangeList = removeAttributes.stream().map(x -> (wekaData.attribute(x).index() + 1) + "").collect(Collectors.joining(","));
				Remove remove = new Remove();
				remove.setAttributeIndices(rangeList);
				remove.setInputFormat(wekaData);
				Instances cleanWekaData = Filter.useFilter(wekaData, remove);

				Integer classIndex = null;
				String targetName = input.getData_set().getTarget_feature();
				for (int i = 0; i < cleanWekaData.numAttributes(); i++) {
					if (cleanWekaData.attribute(i).name().equals(targetName)) {
						classIndex = i;
					}
				}

				if (classIndex == null) {
					logger.error("Could not find target attribute with name {}. Assuming last column to be the target instead.", targetName);
					classIndex = cleanWekaData.numAttributes() - 1;
				}
				cleanWekaData.setClassIndex(classIndex);

				dataset = new WekaInstances(cleanWekaData);
			}
		}

		return SplitterUtil.getRealizationOfSplitSpecification(dataset, Arrays.asList(fitFold, predictFold));
	}

	private static void runMLPlan(final CommandLine cl) throws Exception {
		// check whether a dataset is provided for fitting.
		if (!cl.hasOption(O_FIT_DATASET) && !cl.hasOption(O_OPENML_TASK)) {
			System.err.println("Either need a training dataset provided via " + O_FIT_DATASET + " or a task and fold of an OpenML task provided via " + O_OPENML_TASK);
			System.exit(1);
		} else if (cl.hasOption(O_FIT_DATASET) && cl.hasOption(O_OPENML_TASK)) {
			System.err.println("Cannot use both: local dataset and openml task. Only one option either " + O_FIT_DATASET + " or " + O_OPENML_TASK + " may be given.");
		}

		if (cl.hasOption(O_TMP)) {
			ConfigCache.getOrCreate(IScikitLearnWrapperConfig.class).setProperty(IScikitLearnWrapperConfig.K_TEMP_FOLDER, cl.getOptionValue(O_TMP));
		}

		if (cl.hasOption(O_PYTHON_CMD)) {
			ConfigCache.getOrCreate(IScikitLearnWrapperConfig.class).setProperty(IPythonConfig.KEY_PYTHON, cl.getOptionValue(O_PYTHON_CMD));
			ConfigCache.getOrCreate(IPythonConfig.class).setProperty(IPythonConfig.KEY_PYTHON, cl.getOptionValue(O_PYTHON_CMD));
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
			int taskID = Integer.parseInt(taskSpec[0]);
			int fold = Integer.parseInt(taskSpec[1]);

			List<ILabeledDataset<ILabeledInstance>> split = loadOpenMLTaskAsTrainTestSplit(taskID, fold);
			fitDataset = split.get(0);
			predictDataset = split.get(1);
		} else {
			if (cl.getOptionValue(O_MODULE, getDefault(O_MODULE)).equals(MLPlan4ScikitLearnRegressionCLIModule.M_RUL)) {
				fitDataset = new ArffDatasetAdapter().readDataset(new File(cl.getOptionValue(O_FIT_DATASET)));
			} else {
				Instances wekaData = new Instances(new FileReader(new File(cl.getOptionValue(O_FIT_DATASET))));
				wekaData.setClassIndex(wekaData.numAttributes() - 1);
				fitDataset = new WekaInstances(wekaData);
			}
		}

		// retrieve builder from module
		AMLPlanBuilder builder = module.getMLPlanBuilderForSetting(cl, fitDataset);

		// set common configs
		builder.withNumCpus(Integer.parseInt(cl.getOptionValue(O_NUM_CPUS, getDefault(O_NUM_CPUS))));
		builder.withSeed(Long.parseLong(cl.getOptionValue(O_SEED, getDefault(O_SEED))));

		// set timeouts
		builder.withTimeOut(new Timeout(Integer.parseInt(cl.getOptionValue(O_TIMEOUT, getDefault(O_TIMEOUT))), DEF_TIME_UNIT));
		if (cl.hasOption(O_CANDIDATE_TIMEOUT)) {
			builder.withCandidateEvaluationTimeOut(new Timeout(Integer.parseInt(cl.getOptionValue(O_CANDIDATE_TIMEOUT)), DEF_TIME_UNIT));
		} else {
			Timeout candidateTimeout;
			if (builder.getTimeOut().seconds() <= 60 * 15) {
				candidateTimeout = new Timeout(30, DEF_TIME_UNIT);
			} else if (builder.getTimeOut().seconds() <= 2 * 60 * 60) {
				candidateTimeout = new Timeout(300, DEF_TIME_UNIT);
			} else if (builder.getTimeOut().seconds() < 60 * 60 * 12) {
				candidateTimeout = new Timeout(600, DEF_TIME_UNIT);
			} else {
				candidateTimeout = new Timeout(1200, DEF_TIME_UNIT);
			}
			builder.withCandidateEvaluationTimeOut(candidateTimeout);
		}
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

		StatisticsListener statsListener = new StatisticsListener();
		mlplan.registerListener(statsListener);

		if (cl.hasOption(O_VISUALIZATION)) {
			AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(mlplan);
			window.withMainPlugin(new GraphViewPlugin());
			window.withPlugin(new NodeInfoGUIPlugin("Node Info", new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new NodeInfoGUIPlugin("CI View", new TFDNodeAsCIViewInfoGenerator(builder.getComponents())),
					new SearchRolloutHistogramPlugin(), new SearchRolloutBoxplotPlugin());
		}

		// call ml-plan to obtain the optimal supervised learner
		logger.info("Running ML-Plan ...");
		ISupervisedLearner optimizedLearner = mlplan.call();
		incumbent = mlplan.getComponentInstanceOfSelectedClassifier();
		logger.info("ML-Plan finished. JSON description of selected solution: {}", incumbent);

		if (predictDataset != null || cl.hasOption(O_PREDICT_DATASET)) {
			if (cl.hasOption(O_PREDICT_DATASET)) {
				File predictDatasetFile = new File(cl.getOptionValue(O_PREDICT_DATASET));
				logger.info("Load test data file: {}", predictDatasetFile.getAbsolutePath());
				predictDataset = new ArffDatasetAdapter().readDataset(predictDatasetFile);
			}

			ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(optimizedLearner, predictDataset);
			logger.info("Run report of the module: {}", module.getRunReportAsString(mlplan.getSelectedClassifier(), runReport));
			testPerformance = builder.getMetricForSearchPhase().loss(runReport.getPredictionDiffList());

			if (cl.hasOption(O_OUT_OPENML_BENCHMARK)) {
				String outputFile = cl.getOptionValue(O_OUT_OPENML_BENCHMARK, getDefault(O_OUT_OPENML_BENCHMARK));
				logger.info("Generating report conforming the OpenML AutoML Benchmark format which is then written to {}.", outputFile);
				writeFile(outputFile, new OpenMLAutoMLBenchmarkReport(runReport).toString());
			}

			if (cl.hasOption(O_OUT_STATS)) {
				String outputFile = cl.getOptionValue(O_OUT_STATS, getDefault(O_OUT_STATS));
				logger.info("Generating statistics report in json and writing it to file {}.", outputFile);
				writeFile(outputFile, new StatisticsReport(statsListener, mlplan.getComponentInstanceOfSelectedClassifier(), runReport).toString());
			}
		}

		if (cl.hasOption(O_OUT_MODEL)) {
			String outputFile = cl.getOptionValue(O_OUT_MODEL, getDefault(O_OUT_MODEL));
			logger.info("Serializing trained model of selected classifier {} to output file {}.", optimizedLearner, outputFile);
			FileUtil.serializeObject(optimizedLearner, outputFile);
			logger.info("Serialization completed.");
		}
	}

	public static IComponentInstance incumbent() {
		return incumbent;
	}

	public static Double testPerformance() {
		return testPerformance;
	}

	private static void writeFile(final String fileName, final String value) {
		File file = new File(fileName);
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(value);
		} catch (IOException e) {
			logger.error("Could not write value to file {}: {}", fileName, value);
		}
	}

	public static void main(final String[] args) throws Exception {
		String logLevel;
		if (logger.isTraceEnabled()) {
			logLevel = "TRACE";
		} else if (logger.isDebugEnabled()) {
			logLevel = "DEBUG";
		} else if (logger.isInfoEnabled()) {
			logLevel = "INFO";
		} else if (logger.isWarnEnabled()) {
			logLevel = "WARN";
		} else if (logger.isErrorEnabled()) {
			logLevel = "ERROR";
		} else {
			logLevel = "UNKNOWN";
		}
		logger.info("Logger works properly. Log-level is {}.", logLevel);

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
