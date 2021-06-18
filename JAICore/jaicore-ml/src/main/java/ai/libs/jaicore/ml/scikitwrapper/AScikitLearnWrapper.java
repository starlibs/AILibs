package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.Timeout;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.processes.EOperatingSystem;
import ai.libs.jaicore.processes.ProcessIDNotRetrievableException;
import ai.libs.jaicore.processes.ProcessUtil;
import ai.libs.python.IPythonConfig;
import ai.libs.python.PythonRequirementDefinition;

public abstract class AScikitLearnWrapper<P extends IPrediction, B extends IPredictionBatch> extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, P, B> implements IScikitLearnWrapper {

	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_REL = 3;
	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_MAJ = 5;
	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_MIN = 0;
	protected static final String[] PYTHON_REQUIRED_MODULES = { "arff", "numpy", "json", "pickle", "os", "sys", "warnings", "scipy", "sklearn", "pandas" };
	protected static final String[] PYTHON_OPTIONAL_MODULES = {};

	private static final String SCIKIT_LEARN_TEMPLATE = "sklearn/sklearn_template_windows.twig.py";

	private static final String COULD_NOT_RUN_SCIKIT_LEARN_MODEL = "Could not run scikit-learn model.";

	protected Logger logger = LoggerFactory.getLogger(AScikitLearnWrapper.class);
	protected IScikitLearnWrapperConfig scikitLearnWrapperConfig;
	protected IPythonConfig pythonConfig = ConfigFactory.create(IPythonConfig.class);

	protected final String configurationUID;

	protected EScikitLearnProblemType problemType;
	protected String pipeline;
	private String imports;

	protected File modelFile;
	protected ILabeledDataset<ILabeledInstance> data;
	protected int[] targetIndices;
	protected long seed;
	protected Timeout timeout;
	private boolean listenToPidFromProcess; // If true, the PID is obtained from the python process being started by listening to according output.

	protected AScikitLearnWrapper(final EScikitLearnProblemType problemType, final String pipeline, final String imports) throws IOException {
		this.problemType = problemType;
		this.pipeline = pipeline;
		this.imports = imports;
		this.targetIndices = new int[0];

		String hashCode = Hashing.sha256().hashString(this.pipeline, StandardCharsets.UTF_8).toString();
		this.configurationUID = hashCode.startsWith("-") ? hashCode.replace("-", "1") : "0" + hashCode;

		this.listenToPidFromProcess = (ProcessUtil.getOS() == EOperatingSystem.MAC || ProcessUtil.getOS() == EOperatingSystem.LINUX);

		this.scikitLearnWrapperConfig = ConfigCache.getOrCreate(IScikitLearnWrapperConfig.class);
		this.scikitLearnWrapperConfig.getTempFolder().mkdirs();
		this.scikitLearnWrapperConfig.getModelDumpsDirectory().mkdirs();

		new PythonRequirementDefinition(PYTHON_MINIMUM_REQUIRED_VERSION_REL, PYTHON_MINIMUM_REQUIRED_VERSION_MAJ, PYTHON_MINIMUM_REQUIRED_VERSION_MIN, ArrayUtils.addAll(PYTHON_REQUIRED_MODULES, problemType.getPythonRequiredModules()),
				ArrayUtils.addAll(PYTHON_OPTIONAL_MODULES, problemType.getPythonOptionalModules())).check(this.pythonConfig);

		this.setPythonTemplate(ResourceUtil.getResourceAsTempFile(SCIKIT_LEARN_TEMPLATE));
	}

	@Override
	public void setPythonTemplate(final String pythonTemplatePath) throws IOException {
		File scikitTemplate = new File(pythonTemplatePath);

		File scriptFile = this.getSKLearnScriptFile();
		if (!scriptFile.createNewFile()) {
			this.logger.debug("Script file for configuration UID {} already exists in {}", this.configurationUID, scriptFile.getAbsolutePath());
		}
		if (this.scikitLearnWrapperConfig.getDeleteFileOnExit()) {
			scriptFile.deleteOnExit();
		}

		if (this.pipeline == null || this.pipeline.isEmpty()) {
			throw new AssertionError("Pipeline command for learner must be stated.");
		}

		Map<String, Object> templateValues = new HashMap<>();
		templateValues.put("imports", this.imports != null ? this.imports : "");
		templateValues.put("pipeline", this.pipeline);

		JtwigTemplate template = JtwigTemplate.fileTemplate(scikitTemplate);
		JtwigModel model = JtwigModel.newModel(templateValues);
		template.render(model, new FileOutputStream(scriptFile));
	}

	@Override
	public void setModelPath(final String modelPath) throws IOException {
		this.modelFile = new File(modelPath);
	}

	@Override
	public File getModelPath() {
		return this.modelFile;
	}

	@Override
	public void setTargetIndices(final int... targetIndices) {
		this.targetIndices = targetIndices;
	}

	@Override
	public void setSeed(final long seed) {
		this.seed = seed;
	}

	@Override
	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> trainingData) throws TrainingException, InterruptedException {
		try {
			String dataFileName = this.getDataName(trainingData);
			this.data = (ILabeledDataset<ILabeledInstance>) trainingData.createEmptyCopy();
			if (this.doLabelsFitToProblemType(this.data)) {
				File trainingDataFile = this.getOrWriteDataFile(trainingData, dataFileName);
				this.fit(trainingDataFile, dataFileName);
			} else {
				throw new TrainingException("The label of the given data " + trainingData.getRelationName() + " are not suitable for the selected problem type " + this.problemType.getName());
			}
		} catch (DatasetCreationException | ScikitLearnWrapperExecutionFailedException e) {
			throw new TrainingException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	@Override
	public void fit(final String trainingDataName) throws TrainingException, InterruptedException {
		File trainingDataFile = this.getDatasetFile(trainingDataName);
		this.fit(trainingDataFile, trainingDataName);
	}

	private void fit(final File trainingDataFile, final String trainingDataName) throws TrainingException, InterruptedException {
		try {
			File outputFile = this.getOutputFile(trainingDataName);
			if (!outputFile.exists()) {
				this.modelFile = new File(this.scikitLearnWrapperConfig.getModelDumpsDirectory(), this.getModelFileName(trainingDataName));
				String[] trainCommand = this.constructCommandLineParametersForFitMode(this.modelFile, trainingDataFile, outputFile).toCommandArray();
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("{} run train mode {}", Thread.currentThread().getName(), Arrays.toString(trainCommand));
				}
				this.runProcess(trainCommand);
			}
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new TrainingException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	@Override
	public B predict(final ILabeledDataset<? extends ILabeledInstance> testingData) throws PredictionException, InterruptedException {
		try {
			String testingDataName = this.getDataName(testingData);
			File testingDataFile = this.getOrWriteDataFile(testingData, testingDataName);
			this.logger.info("Prediction dataset serialized, now acquiring predictions.");
			return this.predict(testingDataFile, testingDataName);
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new PredictionException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}

	}

	public B predict(final String testingDataName) throws PredictionException, InterruptedException {
		File testingDataFile = this.getDatasetFile(testingDataName);
		return this.predict(testingDataFile, testingDataName);
	}

	private B predict(final File testingDataFile, final String testingDataName) throws PredictionException, InterruptedException {
		try {
			File outputFile = this.getOutputFile(testingDataName);
			if (!outputFile.exists()) {
				String[] testCommand = this.constructCommandLineParametersForPredictMode(this.modelFile, testingDataFile, outputFile).toCommandArray();
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Run test mode with {}", Arrays.toString(testCommand));
				}
				this.runProcess(testCommand);
			}

			return this.handleOutput(outputFile);
		} catch (ScikitLearnWrapperExecutionFailedException | TrainingException e) {
			throw new PredictionException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	@Override
	public B predict(final ILabeledInstance[] testingInstances) throws PredictionException, InterruptedException {
		Objects.requireNonNull(this.modelFile, "Model has not been trained.");
		Objects.requireNonNull(this.data, "Model has not been trained.");

		this.logger.info("Predicting {} instances.", testingInstances.length);
		ILabeledDataset<ILabeledInstance> testingData;
		try {
			testingData = this.data.createEmptyCopy();
		} catch (DatasetCreationException e1) {
			throw new PredictionException("Could not replicate labeled dataset instance", e1);
		}
		Arrays.stream(testingInstances).forEach(testingData::add);
		return this.predict(testingData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public P predict(final ILabeledInstance instance) throws PredictionException, InterruptedException {
		return (P) this.predict(new ILabeledInstance[] { instance }).get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public B fitAndPredict(final ILabeledDataset<? extends ILabeledInstance> trainingData, final ILabeledDataset<? extends ILabeledInstance> testingData) throws TrainingException, PredictionException, InterruptedException {
		try {
			String trainingDataFileName = this.getDataName(trainingData);
			this.data = (ILabeledDataset<ILabeledInstance>) trainingData.createEmptyCopy();
			File trainingDataFile = this.getOrWriteDataFile(trainingData, trainingDataFileName);

			String testingDataFileName = this.getDataName(testingData);
			File testingDataFile = this.getOrWriteDataFile(testingData, testingDataFileName);
			this.logger.info("Prediction dataset serialized, now acquiring predictions.");
			return this.fitAndPredict(trainingDataFile, trainingDataFileName, testingDataFile, testingDataFileName);
		} catch (DatasetCreationException | ScikitLearnWrapperExecutionFailedException e) {
			throw new TrainingException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	public B fitAndPredict(final File trainingDataFile, final String trainingDataName, final File testingDataFile, final String testingDataName) throws TrainingException, PredictionException, InterruptedException {
		try {
			File trainingOutputFile = this.getOutputFile(trainingDataName);
			File testingOutputFile = this.getOutputFile(testingDataName);
			if (!trainingOutputFile.exists() && !testingOutputFile.exists()) {
				String[] fitAndPredictCommand = this.constructCommandLineParametersForFitAndPredictMode(trainingDataFile, trainingOutputFile, testingDataFile, testingOutputFile).toCommandArray();
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("{} run fitAndPredict mode {}", Thread.currentThread().getName(), Arrays.toString(fitAndPredictCommand));
				}
				this.runProcess(fitAndPredictCommand);
			}

			return this.handleOutput(trainingOutputFile, testingOutputFile);
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new TrainingException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	protected String getModelFileName(final String dataFileName) {
		return this.configurationUID + "_" + dataFileName + this.scikitLearnWrapperConfig.getPickleFileExtension();
	}

	@Override
	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	private synchronized File getOrWriteDataFile(final ILabeledDataset<? extends ILabeledInstance> dataset, final String dataFileName) throws ScikitLearnWrapperExecutionFailedException {
		this.logger.debug("Serializing {}x{} dataset to {}", dataset.size(), dataset.getNumAttributes(), dataFileName);

		File dataFile = this.getDatasetFile(dataFileName);
		if (this.scikitLearnWrapperConfig.getDeleteFileOnExit()) {
			dataFile.deleteOnExit();
		}

		if (dataFile.exists()) {
			this.logger.debug("Reusing dataset: {}", dataFileName);
			return dataFile;
		}

		try {
			ArffDatasetAdapter.serializeDataset(dataFile, dataset);
		} catch (IOException e1) {
			throw new ScikitLearnWrapperExecutionFailedException("Could not dump data file for prediction", e1);
		}
		this.logger.debug("Serializating completed.");
		return dataFile;
	}

	private synchronized File getDatasetFile(final String datasetName) {
		return new File(this.scikitLearnWrapperConfig.getTempFolder(), datasetName + ".arff");
	}

	protected abstract boolean doLabelsFitToProblemType(final ILabeledDataset<? extends ILabeledInstance> data);

	protected ScikitLearnWrapperCommandBuilder getCommandBuilder() {
		ScikitLearnWrapperCommandBuilder commandBuilder = new ScikitLearnWrapperCommandBuilder(this.problemType.getScikitLearnCommandLineFlag(), this.getSKLearnScriptFile());
		return this.getCommandBuilder(commandBuilder);
	}

	protected ScikitLearnWrapperCommandBuilder getCommandBuilder(final ScikitLearnWrapperCommandBuilder commandBuilder) {
		commandBuilder.withLogger(this.logger);
		commandBuilder.withSeed(this.seed);
		commandBuilder.withTimeout(this.timeout);
		if (this.pythonConfig != null) {
			commandBuilder.withPythonConfig(this.pythonConfig);
		}
		return commandBuilder;
	}

	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitMode(final File modelFile, final File trainingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitMode();
		commandBuilder.withModelFile(modelFile);
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withTargetIndices(this.targetIndices);
		return commandBuilder;
	}

	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForPredictMode(final File modelFile, final File testingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withPredictMode();
		commandBuilder.withModelFile(modelFile);
		commandBuilder.withPredictDataFile(testingDataFile);
		commandBuilder.withTargetIndices(this.targetIndices);
		commandBuilder.withPredictOutputFile(outputFile);
		return commandBuilder;
	}

	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitAndPredictMode(final File trainingDataFile, final File trainingOutputFile, final File testingDataFile, final File testingOutputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitAndPredictMode();
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withPredictDataFile(testingDataFile);
		commandBuilder.withPredictOutputFile(testingOutputFile);
		commandBuilder.withTargetIndices(this.targetIndices);
		return commandBuilder;
	}

	private void runProcess(final String[] commandLineParameters) throws InterruptedException, ScikitLearnWrapperExecutionFailedException {
		DefaultProcessListener listener = new DefaultProcessListener(this.listenToPidFromProcess);
		try {
			listener.setLoggerName(this.logger.getName() + ".python");
			this.logger.debug("Set logger name of listener to {}. Now starting python process.", listener.getLoggerName());

			if (this.logger.isDebugEnabled()) {
				String call = Arrays.toString(commandLineParameters).replace(",", "");
				this.logger.info("Starting process {}", call.substring(1, call.length() - 1));
			}

			System.out.println(Arrays.toString(commandLineParameters));
			ProcessBuilder processBuilder = new ProcessBuilder(commandLineParameters).directory(this.scikitLearnWrapperConfig.getTempFolder());
			Process process = processBuilder.start();
			this.logger.debug("Started process with PID: {}. Listener is {}", ProcessUtil.getPID(process), listener);
			this.logger.info("Attaching listener {} to process {}", listener, process);
			listener.listenTo(process);
			this.logger.info("Listener attached.");

			if (!listener.getErrorOutput().isEmpty()) {
				if (listener.getErrorOutput().toLowerCase().contains("convergence")) {
					// ignore convergence warning
					this.logger.warn("Learner {} could not converge. Consider increase number of iterations.", this.pipeline);
				} else {
					throw new ScikitLearnWrapperExecutionFailedException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL);
				}
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (ProcessIDNotRetrievableException e) {
			this.logger.warn("Could not retrieve process ID.");
		} catch (Exception e) {
			throw new ScikitLearnWrapperExecutionFailedException(COULD_NOT_RUN_SCIKIT_LEARN_MODEL, e);
		}
	}

	@Override
	public File getOutputFile(final String dataName) {
		return new File(this.scikitLearnWrapperConfig.getModelDumpsDirectory(), this.configurationUID + "_" + dataName + this.scikitLearnWrapperConfig.getResultFileExtension());
	}

	protected abstract B handleOutput(final File outputFile) throws PredictionException, TrainingException;

	protected B handleOutput(final File fitOutputFile, final File predictOutputFile) throws PredictionException, TrainingException {
		return this.handleOutput(predictOutputFile);
	}

	@SuppressWarnings("unchecked")
	protected List<List<Double>> getRawPredictionResults(final File outputFile) throws PredictionException {
		String fileContent = "";
		List<List<Double>> rawLastPredictionResults;
		try {
			/* Parse the result */
			fileContent = FileUtil.readFileAsString(outputFile);
			if (this.scikitLearnWrapperConfig.getDeleteFileOnExit()) {
				Files.delete(outputFile.toPath());
			}
			ObjectMapper objMapper = new ObjectMapper();
			rawLastPredictionResults = objMapper.readValue(fileContent, List.class);
		} catch (IOException e) {
			throw new PredictionException("Could not read result file or parse the json content to a list.", e);
		}

		if (this.logger.isInfoEnabled()) {
			this.logger.info("{}", rawLastPredictionResults.stream().flatMap(List::stream).collect(Collectors.toList()));
		}
		return rawLastPredictionResults;
	}

	@Override
	public void setPythonConfig(final IPythonConfig pythonConfig) {
		this.pythonConfig = pythonConfig;
	}

	@Override
	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig) {
		this.scikitLearnWrapperConfig = scikitLearnWrapperConfig;
	}

	@Override
	public File getSKLearnScriptFile() {
		return new File(this.scikitLearnWrapperConfig.getTempFolder(), this.configurationUID + this.scikitLearnWrapperConfig.getPythonFileExtension());
	}

	@Override
	public File getModelFile() {
		return this.modelFile;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);

	}

	@Override
	public String toString() {
		return this.pipeline;
	}

}
