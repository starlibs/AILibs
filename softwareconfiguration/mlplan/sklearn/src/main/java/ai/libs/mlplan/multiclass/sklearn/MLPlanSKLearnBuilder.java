package ai.libs.mlplan.multiclass.sklearn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.basic.SystemRequirementsNotMetException;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.PreferenceBasedNodeEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

public class MLPlanSKLearnBuilder extends AbstractMLPlanBuilder<ScikitLearnWrapper, MLPlanSKLearnBuilder> {

	private Logger logger = LoggerFactory.getLogger(MLPlanSKLearnBuilder.class);

	private static final String MSG_MODULE_NOT_AVAILABLE = "Could not load python module {}: {}";
	private static final String PYTHON_MINIMUM_REQUIRED_VERSION = "Python 3.5.0";
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_REL = 3;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MAJ = 5;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MIN = 0;

	private static final String[] PYTHON_REQUIRED_MODULES = { "arff", "numpy", "json", "pickle", "os", "sys", "warnings", "scipy", "sklearn" };

	private static final String COMMAND_PYTHON = "python";
	private static final String[] COMMAND_PYTHON_VERSION = { COMMAND_PYTHON, "--version" };
	private static final String[] COMMAND_PYTHON_EXEC = { COMMAND_PYTHON, "-c" };
	private static final String PYTHON_MODULE_NOT_FOUND_ERROR_MSG = "ModuleNotFoundError";

	private static final String RES_SKLEARN_SEARCHSPACE_CONFIG = "automl/searchmodels/sklearn/sklearn-mlplan.json";
	private static final String RES_SKLEARN_UL_SEARCHSPACE_CONFIG = "automl/searchmodels/sklearn/ml-plan-ul.json";
	private static final String FS_SEARCH_SPACE_CONFIG = "conf/mlplan-sklearn.json";

	private static final String RES_SKLEARN_PREFERRED_COMPONENTS = "automl/searchmodels/sklearn/sklearn-preferenceList.txt";
	private static final String FS_SKLEARN_PREFERRED_COMPONENTS = "conf/sklearn-preferenceList.txt";

	/* DEFAULT VALUES FOR THE SCIKIT-LEARN SETTING */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "AbstractClassifier";
	private static final String DEF_PREFERRED_COMPONENT_NAME_PREFIX = "resolveAbstractClassifierWith";

	private static final File DEF_SEARCH_SPACE_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_SKLEARN_SEARCHSPACE_CONFIG, FS_SEARCH_SPACE_CONFIG);
	private static final File DEF_PREFERRED_COMPONENTS = FileUtil.getExistingFileWithHighestPriority(RES_SKLEARN_PREFERRED_COMPONENTS, FS_SKLEARN_PREFERRED_COMPONENTS);

	private static final SKLearnClassifierFactory DEF_CLASSIFIER_FACTORY = new SKLearnClassifierFactory();
	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE,
			new Random(0));
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
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
		super();
		if (!skipSetupCheck) {
			this.checkPythonSetup();
		}
		this.withSearchSpaceConfigFile(DEF_SEARCH_SPACE_CONFIG);
		this.withPreferredComponentsFile(DEF_PREFERRED_COMPONENTS, DEF_PREFERRED_COMPONENT_NAME_PREFIX);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
		this.withClassifierFactory(DEF_CLASSIFIER_FACTORY);
		this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
		this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
		this.withDatasetSplitterForSearchSelectionSplit(DEF_SEARCH_SELECT_SPLITTER);

		// /* configure blow-ups for MCCV */
		double blowUpInSelectionPhase = MathExt.round(1f / DEFAULT_SEARCH_TRAIN_FOLD_SIZE * DEFAULT_SELECTION_NUM_MC_ITERATIONS / DEFAULT_SEARCH_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getAlgorithmConfig().dataPortionForSelection())) / DEFAULT_SELECTION_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));
	}

	/**
	 * Configures ML-Plan to use the search space with unlimited length preprocessing pipelines.
	 * @return The builder object.
	 * @throws IOException Thrown if the search space configuration file cannot be read.
	 */
	public MLPlanSKLearnBuilder withUnlimitedLengthPipelineSearchSpace() throws IOException {
		return this.withSearchSpaceConfigFile(FileUtil.getExistingFileWithHighestPriority(RES_SKLEARN_UL_SEARCHSPACE_CONFIG, FS_SEARCH_SPACE_CONFIG));
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 *
	 * @param preferredComponentsFile The file containing a priority list of component names.
	 * @param preferableCompnentMethodPrefix The prefix of a method's name for refining a complex task to preferable components.
	 * @return The builder object.
	 * @throws IOException Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public MLPlanSKLearnBuilder withPreferredComponentsFile(final File preferredComponentsFile, final String preferableCompnentMethodPrefix) throws IOException {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS, preferredComponentsFile.getAbsolutePath());
		List<String> ordering;
		if (preferredComponentsFile instanceof ResourceFile) {
			ordering = ResourceUtil.readResourceFileToStringList((ResourceFile) preferredComponentsFile);
		} else if (!preferredComponentsFile.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", preferredComponentsFile.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(preferredComponentsFile);
		}
		return this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.getComponents(), ordering, preferableCompnentMethodPrefix));
	}

	private void checkPythonSetup() {
		try {
			/* Check whether we have python in the $PATH environment variable and whether the required python version is installed. */
			Process p = new ProcessBuilder().command(COMMAND_PYTHON_VERSION).start();
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

	@Override
	public MLPlanSKLearnBuilder getSelf() {
		return this;
	}

}
