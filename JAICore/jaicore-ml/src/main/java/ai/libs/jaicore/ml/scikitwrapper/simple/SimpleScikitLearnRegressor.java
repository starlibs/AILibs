package ai.libs.jaicore.ml.scikitwrapper.simple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.DefaultProcessListener;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapperConfig;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapperExecutionFailedException;
import ai.libs.jaicore.processes.ProcessUtil;
import ai.libs.python.IPythonConfig;

public class SimpleScikitLearnRegressor extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IRegressionPrediction, IRegressionResultBatch> implements IScikitLearnWrapper {
	private Logger logger = LoggerFactory.getLogger(SimpleScikitLearnRegressor.class);
	private static File tempDir = null;

	private static final IPythonConfig pythonC = ConfigFactory.create(IPythonConfig.class);
	private static final IScikitLearnWrapperConfig sklearnRegressorConfig = ConfigFactory.create(IScikitLearnWrapperConfig.class);

	private static final String PATH_EXECUTABLE_TEMPLATE = "sklearn/sklearn_template_windows.twig.py";
	private ILabeledDataset<? extends ILabeledInstance> trainingData;
	private final String constructorCall;
	private final String imports;

	public SimpleScikitLearnRegressor(final String constructorCall, final String imports) {
		this.constructorCall = constructorCall;
		this.imports = imports;
	}

	private synchronized static File getTempDir() throws IOException {
		if (tempDir == null) {
			tempDir = Files.createTempDirectory("ailibs-dumps").toFile();
			tempDir.deleteOnExit();
		}
		return tempDir;
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		this.trainingData = dTrain;
	}

	@Override
	public IRegressionPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		try {
			@SuppressWarnings("unchecked")
			ILabeledDataset<ILabeledInstance> dTest = (ILabeledDataset<ILabeledInstance>) this.trainingData.createEmptyCopy();
			dTest.add(xTest);
			return (IRegressionPrediction) this.predict(dTest).get(0);
		} catch (InterruptedException e) {
			throw e;
		} catch (DatasetCreationException e) {
			throw new PredictionException("Could not predict due to a DatasetCreationException", e);
		}
	}

	@Override
	public IRegressionResultBatch predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		IRegressionResultBatch batch = null;
		Process p = null;

		try {
			File executable = Files.createTempFile("sklearn-classifier-", ".py").toFile();
			executable.deleteOnExit();

			String template = ResourceUtil.readResourceFileToString(PATH_EXECUTABLE_TEMPLATE);
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
			command.add(pythonC.getPythonCommand());
			command.add(executable.getCanonicalPath());
			command.add("--fit");
			command.add(fitFile.getCanonicalPath());
			command.add("--predict");
			command.add(predictFile.getCanonicalPath());
			command.add("--problem");
			command.add("regression");
			command.add("--predictOutput");
			command.add(predictOutputFile.getCanonicalPath());

			ProcessBuilder pb = new ProcessBuilder(command);
			p = pb.start();
			new DefaultProcessListener().listenTo(p);
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				JsonNode n = new ObjectMapper().readTree(FileUtils.readFileToString(predictOutputFile));
				if (!(n instanceof ArrayNode)) {
					throw new PredictionException("Json file for predictions does not contain an array as root element");
				}

				List<IRegressionPrediction> predictions = new ArrayList<>();
				ArrayNode preds = (ArrayNode) n;
				for (JsonNode pred : preds) {
					predictions.add(new SingleTargetRegressionPrediction(pred.asDouble()));
				}
				batch = new SingleTargetRegressionPredictionBatch(predictions);
			} else {
				throw new PredictionException("Could not execute python classifier. Exited with exit code " + exitValue);
			}
		} catch (InterruptedException e) {
			if (p != null) {
				try {
					ProcessUtil.killProcess(p);
				} catch (IOException e1) {
					throw new PredictionException("Could not kill process spawned for executing the python classifier", e1);
				}
			}
			throw e;
		} catch (IOException e) {
			throw new PredictionException("Could not write executable python file.", e);
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new PredictionException("Could not execute scikit learn wrapper", e);
		}
		return batch;
	}

	private synchronized File getOrWriteDataFile(final ILabeledDataset<? extends ILabeledInstance> dataset, final String dataFileName) throws ScikitLearnWrapperExecutionFailedException, IOException {
		this.logger.debug("Serializing {}x{} dataset to {}", dataset.size(), dataset.getNumAttributes(), dataFileName);

		File dataFile = this.getDatasetFile(dataFileName);
		if (sklearnRegressorConfig.getDeleteFileOnExit()) {
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

	@Override
	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	private synchronized File getDatasetFile(final String datasetName) throws IOException {
		return new File(getTempDir(), datasetName + ".arff");
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
	public void setPythonTemplate(final String pythonTemplatePath) throws IOException {
	}

	@Override
	public void setModelPath(final String modelPath) throws IOException {
	}

	@Override
	public File getModelPath() {
		return null;
	}

	@Override
	public File getOutputFile(final String dataName) {
		return null;
	}

	@Override
	public void setPythonConfig(final IPythonConfig pythonConfig) {
	}

	@Override
	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig) {
	}

	@Override
	public File getSKLearnScriptFile() {
		return null;
	}

	@Override
	public File getModelFile() {
		return null;
	}

	@Override
	public void setTargetIndices(final int... targetIndices) {

	}

	@Override
	public String toString() {
		return this.constructorCall;
	}

	@Override
	public IRegressionResultBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		try {
			ILabeledDataset<ILabeledInstance> testData = (ILabeledDataset<ILabeledInstance>) this.trainingData.createEmptyCopy();
			for (ILabeledInstance iTest : dTest) {
				testData.add(iTest);
			}
			return this.predict(testData);
		} catch (DatasetCreationException e) {
			throw new PredictionException("Could not create dataset for test data.", e);
		}
	}

	@Override
	public void setSeed(final long seed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(final Timeout timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fit(final String trainingDataName) throws TrainingException, InterruptedException {
		// TODO Auto-generated method stub

	}
}
