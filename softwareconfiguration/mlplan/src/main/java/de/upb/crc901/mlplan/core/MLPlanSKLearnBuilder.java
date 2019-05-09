package de.upb.crc901.mlplan.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.basic.ResourceUtil;
import jaicore.basic.sets.SetUtil;

public class MLPlanSKLearnBuilder {

	private Logger logger = LoggerFactory.getLogger(MLPlanSKLearnBuilder.class);

	private static final String PYTHON_REQUIRED_VERSION = "Python 3.6.*";
	private static final String[] PYTHON_REQUIRED_MODULES = { "numpy", "json", "pickle", "os", "sys", "warnings", "scipy.io.arff", "sklearn" };

	private static final String COMMAND_PYTHON = "python";
	private static final String[] COMMAND_PYTHON_VERSION = { COMMAND_PYTHON, "--version" };
	private static final String[] COMMAND_PYTHON_EXEC = { COMMAND_PYTHON, "-c" };
	private static final String PYTHON_MODULE_NOT_FOUND_ERROR_MSG = "ModuleNotFoundError";

	/* DEFAULT VALUES FOR THE SCIKIT-LEARN SETTING */
	private static final String RES_AUTOSKLEARN_SEARCHSPACE_CONFIG = "automl/searchmodels/sklearn/sklearn-mlplan.json";
	private static final String RES_UL_SEARCHSPACE_CONFIG = "automl/searchmodels/sklearn/ml-plan-ul.json";

	private static final String FS_DEFAULT_SEARCH_SPACE_CONFIG = "conf/mlplan-searchspace.json";

	private static final File DEFAULT_SEARCH_SPACE_CONFIG_ASKL = FileUtil.getExistingFileWithHighestPriority(RES_AUTOSKLEARN_SEARCHSPACE_CONFIG, FS_DEFAULT_SEARCH_SPACE_CONFIG);
	private static final File DEFAULT_SEARCH_SPACE_CONFIG = ResourceUtil.getResourceAsFile("automl/searchmodels/sklearn/sklearn-mlplan.json");

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 */
	public MLPlanSKLearnBuilder() {
		this(false);
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @param skipSetupCheck Flag whether to skip the system's setup check, which examines whether the operating system has python installed in the required version and all the required python modules are installed.
	 */
	public MLPlanSKLearnBuilder(final boolean skipSetupCheck) {

		if (!skipSetupCheck) {
			this.checkPythonSetup();
		}
	}

	private void checkPythonSetup() {
		try {
			/* Check whether we have python in the $PATH environment variable and whether the required python version is installed. */
			{
				Process p = new ProcessBuilder().command(COMMAND_PYTHON_VERSION).start();
				StringBuilder sb = new StringBuilder();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
				}
				String versionString = sb.toString();
				String regEx = PYTHON_REQUIRED_VERSION.replaceAll("\\.", "\\\\\\.").replaceAll("\\*", "[0-9]");
				if (!versionString.matches(regEx)) {
					throw new SystemRequirementsNotMetException("The installed python version does not match the required " + PYTHON_REQUIRED_VERSION);
				}
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
			try {
				Process allModulesCheckProcess = new ProcessBuilder().command(checkAllModulesAvailableCommand.toArray(new String[0])).start();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(allModulesCheckProcess.getErrorStream()))) {
					String line;
					while ((line = br.readLine()) != null) {
						allModulesAvailableErrorSB.append(line);
					}
				}
			} catch (Exception e) {
				throw new SystemRequirementsNotMetException("Could not check whether the required python modules are installed.", e);
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
						this.logger.debug("Could not load python module {}: {}", module, errorSB);
						modulesNotFound.add(module);
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

}
