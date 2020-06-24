package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.processes.OS;
import ai.libs.jaicore.processes.ProcessIDNotRetrievableException;
import ai.libs.jaicore.processes.ProcessUtil;

/**
 * Wraps a Scikit-Learn Python process by utilizing a template to start a classifier in Scikit with the given classifier.
 *
 * Usage:
 * Set the constructInstruction to exactly the command how the classifier should be instantiated. E.g. "LinearRegression()" or "MLPRegressor(solver = 'lbfg')".
 *
 * Set the imports to exactly what the additional imports lines that are necessary to run the construction command must look like. It is up to the user to decide whether fully
 * qualified names or only the class name themself are used as long as the import is on par with the construct call.
 * E.g (without namespace in construct call) "from sklearn.linear_model import LinearRegression" or (without namespace) "import sklearn.linear_model"
 * createImportStatementFromImportFolder might help to import an own folder of modules. It initializes the folder to be utilizable as a source of modules.
 * Depending on the shape of the construct call the keepNamespace flag must be set (as described above).
 *
 * Before starting the classification it must be set whether the given dataset is a categorical or a regression task (setIsRegression).
 *
 * If the task is a multi target prediction, setTargets must be used to define which columns of the dataset are the targets.
 * If no targets are defined it is assumed that only the last column is the target vector.
 *
 * Moreover, the outputFolder might be set to something else but the default (setOutputFolder).
 *
 * Now buildClassifier can be run.
 *
 * If classifyInstances is run with the same ScikitLearnWrapper instance after training, the previously trained model is used for testing.
 * If another model shall be used or there was no training prior to classifyInstances, the model must be set with setModelPath.
 *
 * After a multi target prediction the results might be more accessible with the unflattened representation that can be obtained with getRawLastClassificationResults.
 * For debug purposes the wrapper might be set to be verbose with setIsVerbose.
 *
 * @author wever
 * @author scheiblm
 */
public class ScikitLearnWrapper<P extends IPrediction, B extends IPredictionBatch> extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, P, B>
		implements ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> {
	private static final String PYTHON_FILE_EXT = ".py";
	private static final String MODEL_DUMP_FILE_EXT = ".pcl";
	private static final String RESULT_FILE_EXT = ".json";

	private static final Logger L = LoggerFactory.getLogger(ScikitLearnWrapper.class);

	private static final File TMP_FOLDER = new File("tmp"); // Folder to put the serialized arff files and the scripts in.

	private static final File MODEL_DUMPS_DIRECTORY = new File(TMP_FOLDER, "model_dumps");
	private static final boolean VERBOSE = true; // If true the output stream of the python process is printed.
	private boolean listenToPidFromProcess; // If true, the PID is obtained from the python process being started by listening to according output.
	private static final boolean DELETE_TEMPORARY_FILES_ON_EXIT = false;

	private File scikitTemplate; // Path to the used python template.
	private ILabeledDataset<ILabeledInstance> dataset;

	/* Problem definition fields */
	private EBasicProblemType problemType;
	private String pathVariable;
	private int[] targetColumns = new int[0]; // Defines which of the columns in the arff file represent the target vectors. If not set, the last column is assumed to be the target vector.

	/* Identifying the wrapped sklearn instance. */
	private final String configurationUID;
	private File modelFile;
	private File trainArff;

	private final boolean withoutModelDump;

	private String constructInstruction;

	/* Since the ScikitLearn is able to do multi-target prediction but Weka is unable to depict it as a result of classifyInstances correctly, this List of
	 * Lists will keep the unflattened results until classifyInstances is called again. classifyInstances will only return a flattened representation of a multi-target prediction.
	 * The outer list represents the rows whilst the inner list represents the x target values in this row.
	 */
	private List<List<Double>> rawLastClassificationResults = null;
	private String anacondaEnvironment;
	private long seed;
	private Timeout timeout;

	/**
	 * Starts a new wrapper and creates its underlying script with the given parameters.
	 *
	 * @param constructInstruction String that defines what constructor to call for the classifier and with which parameters to call it.
	 * @param imports Imports that are appended to the beginning of the script. Normally only the necessary imports for the constructor instruction must be added here.
	 * @throws IOException The script could not be created.
	 */
	public ScikitLearnWrapper(final String constructInstruction, final String imports, final boolean withoutModelDump, final EBasicProblemType problemType) throws IOException {
		if (ProcessUtil.getOS() == OS.MAC || ProcessUtil.getOS() == OS.LINUX) {
			this.listenToPidFromProcess = true;
		} else {
			this.listenToPidFromProcess = false;
		}

		this.withoutModelDump = withoutModelDump;
		this.constructInstruction = constructInstruction;
		this.setProblemType(problemType);

		Map<String, Object> templateValues = this.getTemplateValueMap(constructInstruction, imports);
		String hashCode = StringUtils.join(constructInstruction, imports).hashCode() + "";
		this.configurationUID = hashCode.startsWith("-") ? hashCode.replace("-", "1") : "0" + hashCode;

		if (!TMP_FOLDER.exists()) {
			TMP_FOLDER.mkdirs();
		}

		File scriptFile = this.getSKLearnScriptFile();
		if (!scriptFile.createNewFile() && L.isDebugEnabled()) {
			L.debug("Script file for configuration UID {} already exists in {}", this.configurationUID, scriptFile.getAbsolutePath());
		}
		if (DELETE_TEMPORARY_FILES_ON_EXIT) {
			scriptFile.deleteOnExit();
		}

		/* Prepare SKLearn Script template with the placeholder values */
		JtwigTemplate template = JtwigTemplate.fileTemplate(this.scikitTemplate);
		JtwigModel model = JtwigModel.newModel(templateValues);
		template.render(model, new FileOutputStream(scriptFile));
	}

	/**
	 * Starts a new wrapper and creates its underlying script with the given parameters.
	 *
	 * @param constructInstruction String that defines what constructor to call for the classifier and with which parameters to call it.
	 * @param imports Imports that are appended to the beginning of the script. Normally only the necessary imports for the constructor instruction must be added here.
	 * @throws IOException The script could not be created.
	 */
	public ScikitLearnWrapper(final String constructInstruction, final String imports, final EBasicProblemType problemType) throws IOException {
		this(constructInstruction, imports, false, problemType);
	}

	public ScikitLearnWrapper(final String constructInstruction, final String imports, final File trainedModelPath, final EBasicProblemType problemType) throws IOException {
		this(constructInstruction, imports, false, problemType);
		this.modelFile = trainedModelPath;
	}

	/**
	 * @return The file holding the python script for the wrapper.
	 */
	private File getSKLearnScriptFile() {
		Objects.requireNonNull(this.configurationUID);
		return new File(TMP_FOLDER, this.configurationUID + PYTHON_FILE_EXT);
	}

	/**
	 * @param arffName The name of the test arff file.
	 * @return The file where the results are to be stored.
	 */
	private File getResultFile(final String arffName) {
		return new File(MODEL_DUMPS_DIRECTORY, arffName + "_" + this.configurationUID + RESULT_FILE_EXT);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> data) throws TrainingException, InterruptedException {
		try {
			/* Ensure model dump directory exists and get the name of the dump */
			MODEL_DUMPS_DIRECTORY.mkdirs();
			String arffName = this.getArffName(data);
			this.trainArff = this.getArffFile(data, arffName);
			this.dataset = (ILabeledDataset<ILabeledInstance>) data.createEmptyCopy();

			if (!this.withoutModelDump) {
				this.modelFile = new File(MODEL_DUMPS_DIRECTORY, this.configurationUID + "_" + arffName + MODEL_DUMP_FILE_EXT);
				ScikitLearnWrapper<P, B>.SKLearnWrapperCommandBuilder skLearnWrapperCommandBuilder = new SKLearnWrapperCommandBuilder().withTrainMode().withArffFile(this.trainArff).withOutputFile(this.modelFile);
				if (this.anacondaEnvironment != null) {
					skLearnWrapperCommandBuilder.withAnacondaEnvironment(this.anacondaEnvironment);
				}
				skLearnWrapperCommandBuilder.withSeed(this.seed);
				skLearnWrapperCommandBuilder.withTimeout(this.timeout);
				String[] trainCommand = skLearnWrapperCommandBuilder.toCommandArray();

				if (L.isDebugEnabled()) {
					L.debug("{} run train mode {}", Thread.currentThread().getName(), Arrays.toString(trainCommand));
				}
				DefaultProcessListener listener = new DefaultProcessListener(VERBOSE, this.listenToPidFromProcess);
				this.runProcess(trainCommand, listener);

				if (!listener.getErrorOutput().isEmpty()) {
					L.error("Raise error message: {}", listener.getErrorOutput());
					throw new TrainingException(listener.getErrorOutput().split("\\n")[0]);
				}
			}
		} catch (TrainingException e) {
			throw e;
		} catch (Exception e) {
			throw new TrainingException("An exception occurred while training.", e);
		}
	}

	/**
	 * Dumps given Instances in an arff file if this hash does not already exist.
	 *
	 * @param data Instances to be serialized.
	 * @param fileName Name of the created file.
	 * @return File object corresponding to the arff file.
	 * @throws IOException During the serialization of the data as an arff file something went wrong.
	 */
	private File getArffFile(final ILabeledDataset<? extends ILabeledInstance> data, final String arffName) throws IOException {
		File arffOutputFile = new File(TMP_FOLDER, arffName + ".arff");
		if (DELETE_TEMPORARY_FILES_ON_EXIT) {
			arffOutputFile.deleteOnExit();
		}
		/* If Instances with the same Instance (given the hash is collision resistant) is already serialized, there is no need for doing it once more. */
		if (arffOutputFile.exists()) {
			L.debug("Reusing {}.arff", arffName);
			return arffOutputFile;
		}
		ArffDatasetAdapter.serializeDataset(arffOutputFile, data);
		return arffOutputFile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public P predict(final ILabeledInstance instance) throws PredictionException, InterruptedException {
		return (P) this.predict(new ILabeledInstance[] { instance }).get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public B predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		ILabeledDataset<ILabeledInstance> data;
		try {
			data = this.dataset.createEmptyCopy();
		} catch (DatasetCreationException e1) {
			throw new PredictionException("Could not replicate labeled dataset instance", e1);
		}
		Arrays.stream(dTest).forEach(data::add);

		MODEL_DUMPS_DIRECTORY.mkdirs();
		String arffName = this.getArffName(data);
		File testArff;
		try {
			testArff = this.getArffFile(data, arffName);
		} catch (IOException e1) {
			throw new PredictionException("Could not dump arff file for prediction", e1);
		}
		File outputFile = this.getResultFile(arffName);
		outputFile.getParentFile().mkdirs();

		if (!this.withoutModelDump) {
			ScikitLearnWrapper<P, B>.SKLearnWrapperCommandBuilder skLearnWrapperCommandBuilder = new SKLearnWrapperCommandBuilder().withTestMode().withArffFile(testArff).withModelFile(this.modelFile).withOutputFile(outputFile);
			if (this.anacondaEnvironment != null) {
				skLearnWrapperCommandBuilder.withAnacondaEnvironment(this.anacondaEnvironment);
			}
			skLearnWrapperCommandBuilder.withSeed(this.seed);
			skLearnWrapperCommandBuilder.withTimeout(this.timeout);
			String[] testCommand = skLearnWrapperCommandBuilder.toCommandArray();

			if (L.isDebugEnabled()) {
				L.debug("Run test mode with {}", Arrays.toString(testCommand));
			}

			try {
				this.runProcess(testCommand, new DefaultProcessListener(VERBOSE, this.listenToPidFromProcess));
			} catch (IOException e) {
				throw new PredictionException("Could not run scikit-learn classifier.", e);
			}
		} else {
			ScikitLearnWrapper<P, B>.SKLearnWrapperCommandBuilder skLearnWrapperCommandBuilder = new SKLearnWrapperCommandBuilder().withTrainTestMode().withArffFile(this.trainArff).withTestArffFile(testArff).withOutputFile(outputFile);
			if (this.anacondaEnvironment != null) {
				skLearnWrapperCommandBuilder.withAnacondaEnvironment(this.anacondaEnvironment);
			}
			skLearnWrapperCommandBuilder.withSeed(this.seed);
			skLearnWrapperCommandBuilder.withTimeout(this.timeout);
			String[] testCommand = skLearnWrapperCommandBuilder.toCommandArray();
			if (L.isDebugEnabled()) {
				L.debug("Run train test mode with {}", Arrays.toString(testCommand));
			}

			DefaultProcessListener listener = new DefaultProcessListener(VERBOSE, this.listenToPidFromProcess);
			try {
				this.runProcess(testCommand, listener);
				if (!listener.getErrorOutput().isEmpty()) {
					throw new PredictionException(listener.getErrorOutput());
				}
			} catch (InterruptedException | PredictionException e) {
				throw e;
			} catch (Exception e) {
				throw new PredictionException("Could not run scikit-learn classifier.", e);
			}
		}

		String fileContent = "";
		try {
			/* Parse the result */
			fileContent = FileUtil.readFileAsString(outputFile);
			if (DELETE_TEMPORARY_FILES_ON_EXIT) {
				java.nio.file.Files.delete(outputFile.toPath());
			}
			ObjectMapper objMapper = new ObjectMapper();
			this.rawLastClassificationResults = objMapper.readValue(fileContent, List.class);
		} catch (IOException e) {
			throw new PredictionException("Could not read result file or parse the json content to a list", e);
		}

		/* Since Scikit supports multiple target results but Weka does not, the results have to be flattened.
		 * The structured results of the last classifyInstances call is accessable over
		 * getRawLastClassificationResults().
		 * */
		if (this.problemType == EBasicProblemType.CLASSIFICATION) {
			return (B) new SingleLabelClassificationPredictionBatch(this.rawLastClassificationResults.stream().flatMap(List::stream).map(x -> new SingleLabelClassification((int) (double) x)).collect(Collectors.toList()));
		} else if (this.problemType == EBasicProblemType.RUL) {
			if (L.isInfoEnabled()) {
				L.info("{}", this.rawLastClassificationResults.stream().flatMap(List::stream).collect(Collectors.toList()));
			}
			L.debug("#Created construction string: {}", this.constructInstruction);
			return (B) new SingleTargetRegressionPredictionBatch(this.rawLastClassificationResults.stream().flatMap(List::stream).map(x -> new SingleTargetRegressionPrediction((double) x)).collect(Collectors.toList()));
		}
		throw new PredictionException("Unknown Problem Type.");
	}

	/**
	 * Makes the given folder a module to be usable as an import for python and creates a string that adds the folder to the python environment and then imports the folder itself as a module.
	 *
	 * @param importsFolder Folder to be added as a module.
	 * @param keepNamespace If true, a class must be called by the modules' name plus the class name. This is only important if multiple modules are imported and the classes' names are
	 *            ambiguous. Keep in mind that the constructor call for the classifier must be created accordingly.
	 * @return String which can be appended to other imports to care for the folder to be added as a module.
	 * @throws IOException The __init__.py couldn't be created in the given folder (which is necessary to declare it as a module).
	 */
	public static String createImportStatementFromImportFolder(final File importsFolder, final boolean keepNamespace) throws IOException {
		if (importsFolder == null || !importsFolder.exists() || importsFolder.list().length == 0) {
			return "";
		}
		/* Make the folder a module. */
		if (!Arrays.asList(importsFolder.list()).contains("__init__.py")) {
			File initFile = new File(importsFolder, "__init__.py");
			if (!initFile.createNewFile() && L.isDebugEnabled()) {
				L.debug("Init file {} exists already", initFile.getAbsolutePath());
			}
		}
		StringBuilder result = new StringBuilder();
		String absoluteFolderPath = importsFolder.getAbsolutePath();
		/* Add the folder to the environment of the python script */
		result.append("\n");
		result.append("sys.path.append(r'" + absoluteFolderPath + "')\n");
		for (File module : importsFolder.listFiles()) {
			if (!module.getName().startsWith("__")) {
				/* Either import the module by its name. Then the classes of it have to be referenced by the fully qualified name. */
				if (keepNamespace) {
					result.append("import " + module.getName().substring(0, module.getName().length() - 3) + "\n");
				}
				/*
				 * ... else all the content of the module is imported. Than they can be called
				 * by only their name but therefore there should not be multiple modules
				 * imported that overlap in class names.
				 */
				else {
					result.append("from " + module.getName().substring(0, module.getName().length() - 3) + " import *\n");
				}
			}
		}
		return result.toString();
	}

	/**
	 * Returns a map with the values for the script template.
	 *
	 * @param constructInstruction String that defines what constructor to call for the classifier and with which parameters to call it.
	 * @param imports Imports that are appended to the beginning of the script. Normally only the necessary imports for the constructor instruction must be added here.
	 * @return A map to call the template engine with.
	 */
	private Map<String, Object> getTemplateValueMap(final String constructInstruction, final String imports) {
		if (constructInstruction == null || constructInstruction.isEmpty()) {
			throw new AssertionError("Construction command for classifier must be stated.");
		}
		Map<String, Object> templateValues = new HashMap<>();
		templateValues.put("imports", imports != null ? imports : "");
		templateValues.put("classifier_construct", constructInstruction);
		return templateValues;
	}

	public static String getImportString(final Collection<String> imports) {
		return (imports == null || imports.isEmpty()) ? "" : "import " + StringUtils.join(imports, "\nimport ");
	}

	public List<List<Double>> getRawLastClassificationResults() {
		return this.rawLastClassificationResults;
	}

	public void setProblemType(final EBasicProblemType problemType) {
		if (this.problemType != problemType) {
			this.problemType = problemType;
			this.scikitTemplate = new File(ResourceUtil.getResourceAsTempFile(this.problemType.getRessourceScikitTemplate()));
		}
	}

	public void setPathVariable(final String pathVariable) {
		this.pathVariable = pathVariable;
	}

	public void setAnacondaEnvironment(final String env) {
		this.anacondaEnvironment = env;
	}

	public void setSeed(final long seed) {
		this.seed = seed;
	}

	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

	public void setTargets(final int... targetColumns) {
		this.targetColumns = targetColumns;
	}

	public void setModelPath(final File modelFile) {
		this.modelFile = modelFile;
	}

	public File getModelPath() {
		return this.modelFile;
	}

	/**
	 * Returns a hash for the given Instances based on the Weka implementation of hashCode(). Additionally the sign is replaces by an additional 0/1.
	 *
	 * @param data Instances to get a hash code for.
	 * @return A hash for the given Instances.
	 */
	private String getArffName(final ILabeledDataset<? extends ILabeledInstance> data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	/**
	 * Starts a process with the given attributes. The first String in the array is
	 * the executed program.
	 */
	private void runProcess(final String[] parameters, final AProcessListener listener) throws InterruptedException, IOException {
		if (L.isDebugEnabled()) {
			String call = Arrays.toString(parameters).replace(",", "");
			L.debug("Starting process {}", call.substring(1, call.length() - 1));
		}
		ProcessBuilder processBuilder = new ProcessBuilder(parameters).directory(TMP_FOLDER);
		if (this.pathVariable != null) {
			processBuilder.environment().put("PATH", this.pathVariable);
		}
		Process process = processBuilder.start();
		try {
			L.debug("Started process with PID: {}", ProcessUtil.getPID(process));
		} catch (ProcessIDNotRetrievableException e) {
			L.warn("Could not retrieve process ID.");
		}
		listener.listenTo(process);
	}

	public double[] distributionForInstance(final ILabeledInstance instance) {
		throw new UnsupportedOperationException("This method is not yet implemented");
	}

	/**
	 * Enumeration of the different execution modes with which the SKLearn Wrapper script can be executed.
	 *
	 * @author wever
	 */
	private enum WrapperExecutionMode {
		TRAIN("train"), TEST("test"), TRAIN_TEST("traintest");

		private String name;

		private WrapperExecutionMode(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	/**
	 * This class is a utility for building commands for the process builder in order to run the wrapped python script for sklearn.
	 * Furthermore, it will require the relevant information to be set before successfully returning a command list.
	 *
	 * @author wever
	 */
	private class SKLearnWrapperCommandBuilder {

		private static final String ARFF_FLAG = "--arff";
		private static final String TEST_ARFF_FLAG = "--testarff";
		private static final String MODE_FLAG = "--mode";
		private static final String MODEL_FLAG = "--model";
		private static final String OUTPUT_FLAG = "--output";
		private static final String SEED_FLAG = "--seed";

		private String arffFile;
		private String testArffFile;
		private WrapperExecutionMode mode;
		private String modelFile;
		private String outputFile;
		private String anacondaEnvironment;
		private long seed;
		private Timeout timeout;

		private SKLearnWrapperCommandBuilder() {

		}

		public SKLearnWrapperCommandBuilder withTestArffFile(final File testArffFile) {
			this.testArffFile = testArffFile.getAbsolutePath();
			return this;
		}

		public SKLearnWrapperCommandBuilder withTrainMode() {
			return this.withMode(WrapperExecutionMode.TRAIN);
		}

		public SKLearnWrapperCommandBuilder withTestMode() {
			return this.withMode(WrapperExecutionMode.TEST);
		}

		public SKLearnWrapperCommandBuilder withTrainTestMode() {
			return this.withMode(WrapperExecutionMode.TRAIN_TEST);
		}

		private SKLearnWrapperCommandBuilder withMode(final WrapperExecutionMode execMode) {
			this.mode = execMode;
			return this;
		}

		private SKLearnWrapperCommandBuilder withModelFile(final File modelFile) {
			if (!modelFile.exists()) {
				throw new IllegalArgumentException("Model dump does not exist");
			}
			this.modelFile = modelFile.getAbsolutePath();
			return this;
		}

		private SKLearnWrapperCommandBuilder withOutputFile(final File outputFile) {
			this.outputFile = outputFile.getAbsolutePath();
			return this;
		}

		private SKLearnWrapperCommandBuilder withArffFile(final File arffFile) {
			if (!arffFile.exists()) {
				throw new IllegalArgumentException("Arff File does not exist.");
			}
			this.arffFile = arffFile.getAbsolutePath();
			return this;
		}

		private SKLearnWrapperCommandBuilder withAnacondaEnvironment(final String env) {
			this.anacondaEnvironment = env;
			return this;
		}

		private SKLearnWrapperCommandBuilder withSeed(final long seed) {
			this.seed = seed;
			return this;
		}

		private SKLearnWrapperCommandBuilder withTimeout(final Timeout timeout) {
			this.timeout = timeout;
			return this;
		}

		private String[] toCommandArray() {
			Objects.requireNonNull(this.mode);
			Objects.requireNonNull(this.outputFile);
			Objects.requireNonNull(this.arffFile);

			File scriptFile = ScikitLearnWrapper.this.getSKLearnScriptFile();

			if (!scriptFile.exists()) {
				throw new IllegalArgumentException("The wrapped sklearn script " + scriptFile.getAbsolutePath() + " file does not exist");
			}

			List<String> processParameters = new ArrayList<>();
			OS os = ProcessUtil.getOS();
			if (this.anacondaEnvironment != null) {
				if (os == OS.MAC) {
					processParameters.add("source");
					processParameters.add("~/anaconda3/etc/profile.d/conda.sh");
					processParameters.add("&&");
				}
				processParameters.add("conda");
				processParameters.add("activate");
				processParameters.add(this.anacondaEnvironment);
				processParameters.add("&&");
			}
			if (this.timeout != null && os == OS.LINUX) {
				L.info("Executing with timeout {}s", this.timeout.seconds());
				processParameters.add("timeout");
				processParameters.add(this.timeout.seconds() - 5 + "");
			}
			processParameters.add("python");
			processParameters.add("-u"); // Force python to run stdout and stderr unbuffered.
			processParameters.add(scriptFile.getAbsolutePath()); // Script to be executed.

			// set mode, output, and arff
			processParameters.addAll(Arrays.asList(MODE_FLAG, this.mode.toString()));
			processParameters.addAll(Arrays.asList(ARFF_FLAG, this.arffFile));
			if (this.testArffFile != null) {
				processParameters.addAll(Arrays.asList(TEST_ARFF_FLAG, this.testArffFile));
			}
			processParameters.addAll(Arrays.asList(OUTPUT_FLAG, this.outputFile));
			processParameters.add(ScikitLearnWrapper.this.problemType.getScikitLearnCommandLineFlag());
			processParameters.addAll(Arrays.asList(SEED_FLAG, String.valueOf(this.seed)));

			if (this.mode == WrapperExecutionMode.TEST) {
				Objects.requireNonNull(this.modelFile);
				processParameters.addAll(Arrays.asList(MODEL_FLAG, this.modelFile));
			}

			if (ScikitLearnWrapper.this.targetColumns != null && ScikitLearnWrapper.this.targetColumns.length > 0) {
				processParameters.add("--targets");
				for (int i : ScikitLearnWrapper.this.targetColumns) {
					processParameters.add("" + i);
				}
			}
			/* All additional parameters that the script shall consider. */
			StringJoiner stringJoiner = new StringJoiner(" ");
			for (String parameter : processParameters) {
				stringJoiner.add(parameter);
			}
			if (os == OS.MAC) {
				return new String[] { "sh", "-c", stringJoiner.toString() };
			} else {
				return processParameters.toArray(new String[] {});
			}
		}
	}

	@Override
	public String toString() {
		return this.constructInstruction;
	}

}
