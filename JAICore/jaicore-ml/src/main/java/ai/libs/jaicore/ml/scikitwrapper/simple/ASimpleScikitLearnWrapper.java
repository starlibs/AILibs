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
import ai.libs.python.PythonUtil;

public abstract class ASimpleScikitLearnWrapper<P extends IPrediction, B extends IPredictionBatch> extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, P, B> implements IScikitLearnWrapper {
	private Logger logger = LoggerFactory.getLogger(ASimpleScikitLearnWrapper.class);

	private static File tempDir = null;

	private String pathExecutableTemplate = "sklearn/sklearn_template_windows.twig.py";
	protected IScikitLearnWrapperConfig sklearnClassifierConfig = ConfigFactory.create(IScikitLearnWrapperConfig.class);
	protected IPythonConfig pythonC = ConfigFactory.create(IPythonConfig.class);

	protected final String problem;
	protected final String constructorCall;
	protected final String imports;
	private PythonUtil putil;

	protected ILabeledDataset<? extends ILabeledInstance> trainingData;

	public ASimpleScikitLearnWrapper(final String constructorCall, final String imports, final String problem) {
		this.constructorCall = constructorCall;
		this.imports = imports;
		this.problem = problem;
		this.putil = new PythonUtil(this.pythonC);
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
		File executable = Files.createTempFile("sklearn-classifier-", ".py").toFile();
		executable.deleteOnExit();

		String template = ResourceUtil.readResourceFileToString(this.pathExecutableTemplate);
		template = template.replace("{{pipeline}}", this.constructorCall);
		template = template.replace("{{import}}", this.imports);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(executable))) {
			bw.write(template);
		}
		File predictOutputFile = Files.createTempFile("sklearn-predictions", ".json").toFile();
		predictOutputFile.deleteOnExit();

		File fitFile = this.getOrWriteDataFile(this.trainingData, this.getDataName(this.trainingData));
		File predictFile = this.getOrWriteDataFile(dTest, this.getDataName(dTest));

		List<String> command = new ArrayList<>();
		command.add(executable.getCanonicalPath());
		command.add("--fit");
		command.add(fitFile.getCanonicalPath());
		command.add("--predict");
		command.add(predictFile.getCanonicalPath());
		command.add("--problem");
		command.add(this.problem);
		command.add("--predictOutput");
		command.add(predictOutputFile.getCanonicalPath());

		int exitCode = this.putil.executeScriptFile(command);

		if (exitCode != 0) {
			throw new ScikitLearnWrapperExecutionFailedException("Spawned python process has not terminated cleanly.");
		}
		return predictOutputFile;
	}

	@Override
	public void setModelPath(final String modelPath) throws IOException {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support model serialization.");
	}

	@Override
	public File getModelPath() {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support model serialization.");
	}

	@Override
	public File getModelFile() {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support model serialization.");
	}

	@Override
	public void setTargetIndices(final int... targetIndices) {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support multiple targets.");
	}

	@Override
	public String toString() {
		return this.constructorCall;
	}

	@Override
	public void setSeed(final long seed) {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support setting a seed.");
	}

	@Override
	public void setTimeout(final Timeout timeout) {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support setting a timeout.");
	}

	@Override
	public void fit(final String trainingDataName) throws TrainingException, InterruptedException {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support fitting providing a path only.");
	}

	@Override
	public File getOutputFile(final String dataName) {
		throw new UnsupportedOperationException("The simple scikit-learn classifier wrapper does not support retrieving the output file.");
	}

	@Override
	public void setPythonTemplate(final String pythonTemplatePath) throws IOException {
		this.pathExecutableTemplate = pythonTemplatePath;
	}

	@Override
	public void setPythonConfig(final IPythonConfig pythonConfig) {
		this.pythonC = pythonConfig;
		this.putil = new PythonUtil(pythonConfig);
	}

	@Override
	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig) {
		this.sklearnClassifierConfig = scikitLearnWrapperConfig;
	}

	@Override
	public File getSKLearnScriptFile() {
		return new File(this.pathExecutableTemplate);
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
