package ai.libs.jaicore.ml.scikitwrapper.simple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapperConfig;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapperExecutionFailedException;
import ai.libs.python.IPythonConfig;
import ai.libs.python.PythonRequirementDefinition;
import ai.libs.python.PythonUtil;

public abstract class ASimpleScikitLearnWrapper<P extends IPrediction, B extends IPredictionBatch> extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, P, B> implements IScikitLearnWrapper {
	// logging
	private Logger logger = LoggerFactory.getLogger(ASimpleScikitLearnWrapper.class);

	// python requirements
	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_REL = 3;
	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_MAJ = 5;
	public static final int PYTHON_MINIMUM_REQUIRED_VERSION_MIN = 0;
	protected static final String[] PYTHON_REQUIRED_MODULES = { "arff", "numpy", "json", "pickle", "os", "sys", "warnings", "scipy", "sklearn", "pandas" };
	protected static final String[] PYTHON_OPTIONAL_MODULES = {};

	private static Boolean pythonRequirementsFulfilled = null;

	// configurables
	private static File tempDir = null;

	private String pathExecutableTemplate = "sklearn/sklearn_template_windows.twig.py";
	protected IScikitLearnWrapperConfig sklearnClassifierConfig = ConfigFactory.create(IScikitLearnWrapperConfig.class);
	protected IPythonConfig pythonC;
	private PythonUtil putil;

	// variables of the object
	protected final String problem;
	protected final String constructorCall;
	protected final String imports;

	// temporary files
	private File executable = null;
	private File outputFile = null;

	// temporary data
	protected ILabeledDataset<? extends ILabeledInstance> trainingData;

	public ASimpleScikitLearnWrapper(final String constructorCall, final String imports, final String problem) throws IOException, InterruptedException {
		this(constructorCall, imports, problem, ConfigFactory.create(IPythonConfig.class));
	}

	public ASimpleScikitLearnWrapper(final String constructorCall, final String imports, final String problem, final IPythonConfig pythonConfig) throws IOException, InterruptedException {
		this.constructorCall = constructorCall;
		this.imports = imports;
		this.problem = problem;
		this.setPythonConfig(pythonConfig);
	}

	private synchronized void ensurePythonRequirementsAreSatisfied() throws IOException, InterruptedException {
		if (pythonRequirementsFulfilled == null) {
			new PythonRequirementDefinition(PYTHON_MINIMUM_REQUIRED_VERSION_REL, PYTHON_MINIMUM_REQUIRED_VERSION_MAJ, PYTHON_MINIMUM_REQUIRED_VERSION_MIN, PYTHON_REQUIRED_MODULES, PYTHON_OPTIONAL_MODULES).check(this.pythonC);
		}
		pythonRequirementsFulfilled = true;
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		this.trainingData = dTrain;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	private synchronized File getOrWriteDataFile(final ILabeledDataset<? extends ILabeledInstance> dataset, final String dataFileName) throws ScikitLearnWrapperExecutionFailedException, IOException {
		this.logger.debug("Serializing {}x{} dataset to {}", dataset.size(), dataset.getNumAttributes(), dataFileName);

		File dataFile = this.getDatasetFile(dataFileName);
		if (this.sklearnClassifierConfig.getDeleteFileOnExit()) {
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

	private synchronized File getDatasetFile(final String datasetName) throws IOException {
		File datasetFile = new File(getTempDir(), datasetName + ".arff");
		if (this.sklearnClassifierConfig.getDeleteFileOnExit()) {
			datasetFile.deleteOnExit();
		}
		return datasetFile;
	}

	private static synchronized File getTempDir() throws IOException {
		if (tempDir == null) {
			tempDir = Files.createTempDirectory("ailibs-dumps").toFile();
			tempDir.deleteOnExit();
		}
		return tempDir;
	}

	protected File executePipeline(final ILabeledDataset<? extends ILabeledInstance> dTest) throws IOException, InterruptedException, ScikitLearnWrapperExecutionFailedException {
		this.executable = Files.createTempFile("sklearn-classifier-", ".py").toFile();
		this.executable.deleteOnExit();

		String template = ResourceUtil.readResourceFileToString(this.pathExecutableTemplate);
		template = template.replace("{{pipeline}}", this.constructorCall);
		template = template.replace("{{import}}", this.imports);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.executable))) {
			bw.write(template);
		}
		this.outputFile = Files.createTempFile("sklearn-predictions", ".json").toFile();
		this.outputFile.deleteOnExit();

		File fitFile = this.getOrWriteDataFile(this.trainingData, this.getDataName(this.trainingData));
		File predictFile = this.getOrWriteDataFile(dTest, this.getDataName(dTest));

		List<String> command = new ArrayList<>();
		command.add(this.executable.getCanonicalPath());
		command.add("--fit");
		command.add(fitFile.getCanonicalPath());
		command.add("--predict");
		command.add(predictFile.getCanonicalPath());
		command.add("--problem");
		command.add(this.problem);
		command.add("--predictOutput");
		command.add(this.outputFile.getCanonicalPath());

		int exitCode = this.putil.executeScriptFile(command);

		if (exitCode != 0) {
			throw new ScikitLearnWrapperExecutionFailedException("Spawned python process has not terminated cleanly.");
		}
		return this.outputFile;
	}

	@Override
	public void setModelPath(final String modelPath) throws IOException {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support model serialization.");
	}

	@Override
	public File getModelPath() {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support model serialization.");
		return null;
	}

	@Override
	public File getModelFile() {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support model serialization.");
		return null;
	}

	@Override
	public void setTargetIndices(final int... targetIndices) {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support multiple targets.");
	}

	@Override
	public String toString() {
		return this.constructorCall;
	}

	@Override
	public void setSeed(final long seed) {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support setting a seed.");
	}

	@Override
	public void setTimeout(final Timeout timeout) {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support setting a timeout.");
	}

	@Override
	public void fit(final String trainingDataName) throws TrainingException, InterruptedException {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support fitting providing a path only.");
	}

	@Override
	public File getOutputFile(final String dataName) {
		this.logger.debug("The simple scikit-learn classifier wrapper does not support retrieving the output file.");
		return this.outputFile;
	}

	@Override
	public void setPythonTemplate(final String pythonTemplatePath) throws IOException {
		this.pathExecutableTemplate = pythonTemplatePath;
	}

	@Override
	public void setPythonConfig(final IPythonConfig pythonConfig) throws IOException, InterruptedException {
		this.pythonC = pythonConfig;
		this.putil = new PythonUtil(pythonConfig);
		this.ensurePythonRequirementsAreSatisfied();
	}

	@Override
	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig) {
		this.sklearnClassifierConfig = scikitLearnWrapperConfig;
	}

	@Override
	public File getSKLearnScriptFile() {
		return this.executable;
	}

	@Override
	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	@SuppressWarnings("unchecked")
	@Override
	public B predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		ILabeledDataset<ILabeledInstance> dataset;
		try {
			dataset = (ILabeledDataset<ILabeledInstance>) this.trainingData.createEmptyCopy();
		} catch (InterruptedException e) {
			throw e;
		} catch (DatasetCreationException e) {
			throw new PredictionException("Could not create empty test dataset copy.", e);
		}
		Arrays.stream(dTest).forEach(dataset::add);
		return this.predict(dataset);
	}

	@SuppressWarnings("unchecked")
	@Override
	public P predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		try {
			ILabeledDataset<ILabeledInstance> dTest = (ILabeledDataset<ILabeledInstance>) this.trainingData.createEmptyCopy();
			dTest.add(xTest);
			return (P) this.predict(dTest).get(0);
		} catch (InterruptedException e) {
			throw e;
		} catch (DatasetCreationException e) {
			throw new PredictionException("Could not predict due to a DatasetCreationException", e);
		}
	}
}
