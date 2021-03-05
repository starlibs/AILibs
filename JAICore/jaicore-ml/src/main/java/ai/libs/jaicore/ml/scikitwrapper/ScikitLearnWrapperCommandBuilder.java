package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;

import ai.libs.jaicore.processes.EOperatingSystem;
import ai.libs.jaicore.processes.ProcessUtil;
import ai.libs.python.IPythonConfig;
import ai.libs.python.PythonUtil;

public class ScikitLearnWrapperCommandBuilder {

	private Logger logger;

	private enum EWrapperExecutionMode {
		FIT("fit"), PREDICT("predict"), FIT_AND_PREDICT("fitAndPredict");

		private String name;

		private EWrapperExecutionMode(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private static final String PROBLEM_FLAG = "--problem";
	private static final String MODE_FLAG = "--mode";
	private static final String MODEL_FLAG = "--model";
	private static final String FIT_DATA_FLAG = "--fit";
	private static final String FIT_OUTPUT_FLAG = "--fitOutput";
	private static final String PREDICT_DATA_FLAG = "--predict";
	private static final String PREDICT_OUTPUT_FLAG = "--predictOutput";
	private static final String SEED_FLAG = "--seed";

	private IPythonConfig pythonConfiguration;
	private String problemTypeFlag;
	private File scriptFile;
	private EWrapperExecutionMode executionMode;
	protected String modelFile;
	protected String fitDataFile;
	protected String fitOutputFile;
	protected String predictDataFile;
	protected String predictOutputFile;
	private long seed;
	private Timeout timeout;
	protected List<String> additionalParameters;

	protected ScikitLearnWrapperCommandBuilder(final String problemTypeFlag, final File scriptFile) {
		this.problemTypeFlag = problemTypeFlag;
		this.scriptFile = scriptFile;
	}

	public ScikitLearnWrapperCommandBuilder withPythonConfig(final IPythonConfig pythonConfiguration) {
		this.pythonConfiguration = pythonConfiguration;
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withLogger(final Logger logger) {
		this.logger = logger;
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withScriptFile(final File scriptFile) {
		this.scriptFile = scriptFile;
		return this;
	}

	private ScikitLearnWrapperCommandBuilder withMode(final EWrapperExecutionMode executionMode) {
		this.executionMode = executionMode;
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withFitMode() {
		return this.withMode(EWrapperExecutionMode.FIT);
	}

	public ScikitLearnWrapperCommandBuilder withPredictMode() {
		return this.withMode(EWrapperExecutionMode.PREDICT);
	}

	public ScikitLearnWrapperCommandBuilder withFitAndPredictMode() {
		return this.withMode(EWrapperExecutionMode.FIT_AND_PREDICT);
	}

	public ScikitLearnWrapperCommandBuilder withModelFile(final File modelFile) {
		this.modelFile = modelFile.getAbsolutePath();
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withFitDataFile(final File trainDataFile) {
		if (!trainDataFile.getAbsoluteFile().exists()) {
			throw new IllegalArgumentException("Data file does not exist: " + trainDataFile.getAbsolutePath());
		}
		this.fitDataFile = trainDataFile.getAbsolutePath();
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withFitOutputFile(final File outputFile) {
		this.fitOutputFile = outputFile.getAbsolutePath();
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withPredictDataFile(final File testDataFile) {
		this.predictDataFile = testDataFile.getAbsolutePath();
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withPredictOutputFile(final File outputFile) {
		this.predictOutputFile = outputFile.getAbsolutePath();
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withSeed(final long seed) {
		this.seed = seed;
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withTimeout(final Timeout timeout) {
		this.timeout = timeout;
		return this;
	}

	public ScikitLearnWrapperCommandBuilder withAdditionalCommandLineParameters(final List<String> additionalCommandLineParameters) {
		this.additionalParameters = additionalCommandLineParameters;
		return this;
	}

	public void checkRequirements() {
		if (!this.scriptFile.exists()) {
			throw new IllegalArgumentException("The wrapped sklearn script " + this.scriptFile.getAbsolutePath() + " file does not exist");
		}

		Objects.requireNonNull(this.problemTypeFlag);
		Objects.requireNonNull(this.executionMode);
		switch (this.executionMode) {
		case FIT:
			this.checkRequirementsTrainMode();
			break;
		case PREDICT:
			this.checkRequirementsTestMode();
			break;
		case FIT_AND_PREDICT:
			this.checkRequirementsTrainTestMode();
			break;
		}
	}

	protected void checkRequirementsTrainMode() {
		Objects.requireNonNull(this.fitDataFile);
		Objects.requireNonNull(this.modelFile);
	}

	protected void checkRequirementsTestMode() {
		Objects.requireNonNull(this.modelFile);
		Objects.requireNonNull(this.predictDataFile);
		Objects.requireNonNull(this.predictOutputFile);
	}

	protected void checkRequirementsTrainTestMode() {
		Objects.requireNonNull(this.fitDataFile);
		Objects.requireNonNull(this.predictDataFile);
		Objects.requireNonNull(this.predictOutputFile);
	}

	public String[] toCommandArray() {
		this.checkRequirements();
		List<String> processParameters = new ArrayList<>();
		EOperatingSystem os = ProcessUtil.getOS();
		if (this.timeout != null && os == EOperatingSystem.LINUX) {
			this.logger.info("Executing with timeout {}s", this.timeout.seconds());
			processParameters.add("timeout");
			processParameters.add(this.timeout.seconds() - 2 + "");
		}
		processParameters.add("-u"); // Force python to run stdout and stderr unbuffered.
		processParameters.add(this.scriptFile.getAbsolutePath());
		processParameters.addAll(Arrays.asList(PROBLEM_FLAG, this.problemTypeFlag));
		processParameters.addAll(Arrays.asList(MODE_FLAG, this.executionMode.toString()));
		if (this.modelFile != null) {
			processParameters.addAll(Arrays.asList(MODEL_FLAG, this.modelFile));
		}
		if (this.fitDataFile != null) {
			processParameters.addAll(Arrays.asList(FIT_DATA_FLAG, this.fitDataFile));
		}
		if (this.fitOutputFile != null) {
			processParameters.addAll(Arrays.asList(FIT_OUTPUT_FLAG, this.fitOutputFile));
		}
		if (this.predictDataFile != null) {
			processParameters.addAll(Arrays.asList(PREDICT_DATA_FLAG, this.predictDataFile));
		}
		if (this.predictOutputFile != null) {
			processParameters.addAll(Arrays.asList(PREDICT_OUTPUT_FLAG, this.predictOutputFile));
		}
		processParameters.addAll(Arrays.asList(SEED_FLAG, String.valueOf(this.seed)));
		if (this.additionalParameters != null) {
			processParameters.addAll(this.additionalParameters);
		}
		StringJoiner stringJoiner = new StringJoiner(" ");
		for (String parameter : processParameters) {
			stringJoiner.add(parameter);
		}
		return new PythonUtil(this.pythonConfiguration).getExecutableCommandArray(stringJoiner.toString(), false);
	}

}
