package ai.libs.mlplan.multiclass.sklearn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.SystemRequirementsNotMetException;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;

public abstract class MLPlanSKLearnBuilder<P extends IPrediction, B extends IPredictionBatch> extends AbstractMLPlanBuilder<ScikitLearnWrapper<P, B>, MLPlanSKLearnBuilder<P, B>> {

	private Logger logger = LoggerFactory.getLogger(MLPlanSKLearnBuilder.class);

	private static final String MSG_MODULE_NOT_AVAILABLE = "Could not load python module {}: {}";
	private static final String PYTHON_MINIMUM_REQUIRED_VERSION = "Python 3.5.0";
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_REL = 3;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MAJ = 5;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MIN = 0;

	private static final String[] PYTHON_REQUIRED_MODULES = { "arff", "numpy", "json", "pickle", "os", "sys", "warnings", "scipy", "sklearn" };

	private static final String[] COMMAND_PYTHON_EXEC = { "python", "-c" };
	private static final String[] COMMAND_PYTHON_BASH = { "sh", "-c", "python --version" };

	private static final String PYTHON_MODULE_NOT_FOUND_ERROR_MSG = "ModuleNotFoundError";

	/* DEFAULT VALUES FOR THE SCIKIT-LEARN SETTING */
	private static final EMLPlanSkLearnProblemType DEF_PROBLEM_TYPE = EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS;
	private static final ASKLearnClassifierFactory DEF_CLASSIFIER_FACTORY = new ASKLearnClassifierFactory();
	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE,
			new Random(0));

	private String pathVariable;
	private final boolean skipSetupCheck;

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @throws IOException Thrown if configuration files cannot be read.
	 */
	public MLPlanSKLearnBuilder() throws IOException {
		this(false);
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @param skipSetupCheck Flag whether to skip the system's setup check, which examines whether the operating system has python installed in the required version and all the required python modules are installed.
	 * @throws IOException Thrown if configuration files cannot be read.
	 */
	public MLPlanSKLearnBuilder(final boolean skipSetupCheck) throws IOException {
		super(DEF_PROBLEM_TYPE);
		this.skipSetupCheck = skipSetupCheck;
		this.withLearnerFactory(DEF_CLASSIFIER_FACTORY);
	}

	@SuppressWarnings("unchecked")
	public MLPlanSKLearnBuilder<P, B> withPathVariable(final String path) {
		this.pathVariable = path;
		this.getLearnerFactory().setPathVariable(path);
		return this.getSelf();
	}

	@SuppressWarnings("unchecked")
	public MLPlanSKLearnBuilder<P, B> withAnacondaEnvironment(final String env) {
		this.getLearnerFactory().setAnacondaEnvironment(env);
		return this.getSelf();
	}

	@Override
	@SuppressWarnings("unchecked")
	public MLPlanSKLearnBuilder<P, B> withSeed(final long seed) {
		super.withSeed(seed);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setSeed(seed);
		}
		return this.getSelf();
	}

	@Override
	@SuppressWarnings("unchecked")
	public MLPlanSKLearnBuilder<P, B> withCandidateEvaluationTimeOut(final Timeout timeout) {
		super.withCandidateEvaluationTimeOut(timeout);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setTimeout(timeout);
		}
		return this.getSelf();
	}

	private void checkPythonSetup() {
		try {
			/* Check whether we have python in the $PATH environment variable and whether the required python version is installed. */
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.redirectErrorStream(true);
			if (this.pathVariable != null) {
				processBuilder.environment().put("PATH", this.pathVariable);
			}
			Process p;
			String osName = System.getProperty("os.name").toLowerCase();
			if (osName.contains("mac")) {
				p = processBuilder.command(COMMAND_PYTHON_BASH).start();
			} else {
				p = processBuilder.command(COMMAND_PYTHON_EXEC).start();
			}
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
			String versionString = sb.toString();
			if (!versionString.startsWith("Python ")) {
				throw new SystemRequirementsNotMetException("Could not detect valid python version. (>>" + versionString + "<<)");
			}

			String[] versionSplit = versionString.substring(7).split("\\.");
			if (versionSplit.length != 3) {
				throw new SystemRequirementsNotMetException("Could not parse python version to be of the shape X.X.X");
			}

			int rel = Integer.parseInt(versionSplit[0]);
			int maj = Integer.parseInt(versionSplit[1]);
			int min = Integer.parseInt(versionSplit[2]);

			if (!this.isValidVersion(rel, maj, min)) {
				throw new SystemRequirementsNotMetException("Python version does not conform the minimum required python version of " + PYTHON_MINIMUM_REQUIRED_VERSION);
			}

			/* Check whether we have all required python modules available*/
			List<String> checkAllModulesAvailableCommand = new LinkedList<>(Arrays.asList(COMMAND_PYTHON_EXEC));
			StringBuilder imports = new StringBuilder();
			for (String module : PYTHON_REQUIRED_MODULES) {
				if (!imports.toString().isEmpty()) {
					imports.append(";");
				}
				imports.append("import " + module);
			}
			checkAllModulesAvailableCommand.add(imports.toString());
			StringBuilder allModulesAvailableErrorSB = new StringBuilder();
			Process allModulesCheckProcess = new ProcessBuilder().command(checkAllModulesAvailableCommand.toArray(new String[0])).start();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(allModulesCheckProcess.getErrorStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					allModulesAvailableErrorSB.append(line);
				}
			}

			if (!allModulesAvailableErrorSB.toString().isEmpty()) {
				List<String> modulesNotFound = new LinkedList<>();
				for (String module : PYTHON_REQUIRED_MODULES) {
					Process moduleCheck = new ProcessBuilder().command(COMMAND_PYTHON_EXEC[0], COMMAND_PYTHON_EXEC[1], "import " + module).start();
					StringBuilder errorSB = new StringBuilder();
					try (BufferedReader br = new BufferedReader(new InputStreamReader(moduleCheck.getErrorStream()))) {
						String line;
						while ((line = br.readLine()) != null) {
							errorSB.append(line);
						}
					}
					if (!errorSB.toString().isEmpty() && errorSB.toString().contains(PYTHON_MODULE_NOT_FOUND_ERROR_MSG)) {
						if (module.equals("arff")) {
							this.logger.debug(MSG_MODULE_NOT_AVAILABLE, "liac-arff", errorSB);
							modulesNotFound.add("liac-arff");
						} else if (module.equals("sklearn")) {
							this.logger.debug(MSG_MODULE_NOT_AVAILABLE, "scikit-learn", errorSB);
							modulesNotFound.add("scikit-learn");
						} else {
							this.logger.debug(MSG_MODULE_NOT_AVAILABLE, module, errorSB);
							modulesNotFound.add(module);
						}
					}
				}
				if (!modulesNotFound.isEmpty()) {
					throw new SystemRequirementsNotMetException("Could not find required python modules: " + SetUtil.implode(modulesNotFound, ", "));
				}
			}

		} catch (IOException e) {
			throw new SystemRequirementsNotMetException("Could not check whether python is installed in the required version. Is python available as a command on your command line?");
		}
	}

	private boolean isValidVersion(final int rel, final int maj, final int min) {
		return ((rel > PYTHON_MINIMUM_REQUIRED_VERSION_REL) || (rel == PYTHON_MINIMUM_REQUIRED_VERSION_REL && maj > PYTHON_MINIMUM_REQUIRED_VERSION_MAJ)
				|| (rel == PYTHON_MINIMUM_REQUIRED_VERSION_REL && maj == PYTHON_MINIMUM_REQUIRED_VERSION_MAJ && min >= PYTHON_MINIMUM_REQUIRED_VERSION_MIN));
	}

	//	@Override
	//	public MLPlanSKLearnBuilder<P, B> withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
	//		super.withSearchSpaceConfigFile(searchSpaceConfig);
	//		if (this.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS) == null) {
	//			this.withPreferredComponentsFile(FileUtil.getExistingFileWithHighestPriority(this.problemType.getPreferredComponentListFromResource(), this.problemType.getPreferredComponentListFromFileSystem()),
	//					this.problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner(), true);
	//		} else {
	//			this.withPreferredComponentsFile(new File(this.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS)), this.problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner(), true);
	//		}
	//		return this.getSelf();
	//	}

	@Override
	@SuppressWarnings("unchecked")
	public ASKLearnClassifierFactory getLearnerFactory() {
		return (ASKLearnClassifierFactory) super.getLearnerFactory();
	}

	@Override
	public MLPlanSKLearnBuilder<P, B> getSelf() {
		return this;
	}

	@Override
	public MLPlan<ScikitLearnWrapper<P, B>> build() {
		if (!this.skipSetupCheck) {
			this.checkPythonSetup();
		}
		return super.build();
	}

}
