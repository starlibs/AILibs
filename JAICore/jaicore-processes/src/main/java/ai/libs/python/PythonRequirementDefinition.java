package ai.libs.python;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.SystemRequirementsNotMetException;
import ai.libs.jaicore.basic.sets.SetUtil;

public class PythonRequirementDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(PythonRequirementDefinition.class);

	private final int release;
	private final int major;
	private final int minor;

	private final List<String> requiredModules;
	private final List<String> optionalModules;

	public PythonRequirementDefinition(final int release, final int major, final int minor, final String[] requiredModules, final String[] optionalModules) {
		this(release, major, minor, Arrays.asList(requiredModules), Arrays.asList(optionalModules));
	}

	public PythonRequirementDefinition(final int release, final int major, final int minor, final List<String> requiredModules, final List<String> optionalModules) {
		super();
		this.release = release;
		this.major = major;
		this.minor = minor;
		this.requiredModules = requiredModules;
		this.optionalModules = optionalModules;
	}

	public PythonRequirementDefinition(final int release, final int major, final int minor) {
		this(release, major, minor, new ArrayList<>(), new ArrayList<>());
	}

	public int getRelease() {
		return this.release;
	}

	public int getMajor() {
		return this.major;
	}

	public int getMinor() {
		return this.minor;
	}

	public List<String> getRequiredModules() {
		return this.requiredModules;
	}

	public List<String> getOptionalModules() {
		return this.optionalModules;
	}

	public void check() throws InterruptedException {
		this.check(null);
	}

	public void check(final IPythonConfig pythonConfig) throws InterruptedException {
		try {
			/* Check whether we have all required python modules available*/
			PythonUtil pu = pythonConfig != null ? new PythonUtil(pythonConfig) : new PythonUtil();
			if (!pu.isInstalledVersionCompatible(this.release, this.major, this.minor)) {
				throw new SystemRequirementsNotMetException("Python version does not conform the minimum required python version of " + this.release + "." + this.major + "." + this.minor);
			}
			List<String> missingRequiredPythonModules = pu.getMissingModules(this.requiredModules);
			if (!missingRequiredPythonModules.isEmpty()) {
				throw new SystemRequirementsNotMetException("Could not find required python modules: " + SetUtil.implode(missingRequiredPythonModules, ", "));
			}
			List<String> missingOptionalPythonModules = pu.getMissingModules(this.optionalModules);
			if (!missingOptionalPythonModules.isEmpty() && LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not find optional python modules: {}", SetUtil.implode(missingOptionalPythonModules, ", "));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SystemRequirementsNotMetException("Could not check whether python is installed in the required version. Make sure that \"python\" is available as a command on your command line.");
		}
	}
}
