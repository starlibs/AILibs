package ai.libs.python;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.basic.SystemRequirementsNotMetException;
import ai.libs.jaicore.basic.sets.SetUtil;

public class PythonRequirementDefinition {

	private final int release;
	private final int major;
	private final int minor;

	private final List<String> modules;

	public PythonRequirementDefinition(final int release, final int major, final int minor, final String... modules) {
		this(release, major, minor, Arrays.asList(modules));
	}

	public PythonRequirementDefinition(final int release, final int major, final int minor, final List<String> modules) {
		super();
		this.release = release;
		this.major = major;
		this.minor = minor;
		this.modules = modules;
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

	public List<String> getModules() {
		return this.modules;
	}

	public void check() {
		this.check(null);
	}

	public void check(final IPythonConfig pythonConfig) {
		try {

			/* Check whether we have all required python modules available*/
			PythonUtil pu = pythonConfig != null ? new PythonUtil(pythonConfig) : new PythonUtil();
			if (!pu.isInstalledVersionCompatible(this.release, this.major, this.minor)) {
				throw new SystemRequirementsNotMetException("Python version does not conform the minimum required python version of " + this.release + "." + this.major + "." + this.minor);
			}
			List<String> missingPythonModules = pu.getMissingModules(this.modules);
			if (!missingPythonModules.isEmpty()) {
				throw new SystemRequirementsNotMetException("Could not find required python modules: " + SetUtil.implode(missingPythonModules, ", "));
			}
		} catch (IOException e) {
			throw new SystemRequirementsNotMetException("Could not check whether python is installed in the required version. Make sure that \"python\" is available as a command on your command line.");
		}
	}
}
