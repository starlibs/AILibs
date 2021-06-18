package ai.libs.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.basic.SystemRequirementsNotMetException;
import ai.libs.jaicore.processes.EOperatingSystem;
import ai.libs.jaicore.processes.ProcessUtil;

public class PythonUtil {
	private static final String CMD_PYTHON_COMMANDPARAM = "-c";
	private static final String PY_IMPORT = "import ";

	private final File pathToPythonExecutable;
	private final String pythonCommand;
	private String pathToAnacondaExecutable;
	private String anacondaEnvironment;

	public PythonUtil() {
		this(ConfigCache.getOrCreate(IPythonConfig.class));
	}

	public PythonUtil(final IPythonConfig config) {
		this.pythonCommand = config.getPythonCommand();
		if (config.getAnacondaEnvironment() != null) {
			if (config.getPathToAnacondaExecutable() != null) {
				String path = config.getPathToAnacondaExecutable();
				this.pathToAnacondaExecutable = (path != null) ? path : null;
			}
			this.anacondaEnvironment = config.getAnacondaEnvironment();
		}

		String path = config.getPathToPythonExecutable();
		this.pathToPythonExecutable = (path != null) ? new File(path) : null;

		if (this.pathToPythonExecutable != null) {
			if (!this.pathToPythonExecutable.exists()) {
				throw new IllegalArgumentException("The path to python executable " + this.pathToPythonExecutable.getAbsolutePath() + " does not exist.");
			}
			if (!new File(this.pathToPythonExecutable + File.separator + this.pythonCommand).exists()) {
				throw new IllegalArgumentException("The given path does not contain an executable with name " + this.pythonCommand);
			}
		}
	}

	public ProcessBuilder getProcessBuilder() {
		return new ProcessBuilder();
	}

	public String[] getExecutableCommandArray(final String command, final boolean executePythonInteractive) {
		List<String> processParameters = new ArrayList<>();
		EOperatingSystem os = ProcessUtil.getOS();
		if (this.anacondaEnvironment != null) {
			if (os == EOperatingSystem.MAC) {
				processParameters.add("source");
				processParameters.add(this.pathToAnacondaExecutable);
				processParameters.add("&&");
			}
			processParameters.add("conda");
			processParameters.add("activate");
			processParameters.add(this.anacondaEnvironment);
			processParameters.add("&&");
		}

		if (this.pathToPythonExecutable != null) {
			processParameters.add(this.pathToPythonExecutable + File.separator + this.pythonCommand);
		} else {
			processParameters.add(this.pythonCommand);
		}

		if (executePythonInteractive) {
			processParameters.add(CMD_PYTHON_COMMANDPARAM);
			processParameters.add(command);
		} else {
			processParameters.add(command);
		}

		if (os == EOperatingSystem.MAC) {
			StringJoiner stringJoiner = new StringJoiner(" ");
			for (String parameter : processParameters) {
				stringJoiner.add(parameter);
			}
			return new String[] { "sh", "-c", stringJoiner.toString() };
		} else {
			return processParameters.toArray(new String[] {});
		}
	}

	public String executeScript(final String script) throws IOException {
		String[] command = this.getExecutableCommandArray(script, true);
		ProcessBuilder processBuilder = this.getProcessBuilder();
		processBuilder.redirectErrorStream(true);
		Process p = processBuilder.command(command).start();
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	public String getInstalledVersion() throws IOException {
		return this.executeScript(PY_IMPORT + "platform; print(platform.python_version())");
	}

	public boolean isInstalledVersionCompatible(final int reqRel, final int reqMaj, final int reqMin) throws IOException {
		String[] versionSplit = this.getInstalledVersion().split("\\.");
		if (versionSplit.length != 3) {
			throw new SystemRequirementsNotMetException("Could not parse python version to be of the shape X.X.X");
		}
		int rel = Integer.parseInt(versionSplit[0]);
		int maj = Integer.parseInt(versionSplit[1]);
		int min = Integer.parseInt(versionSplit[2]);
		return this.isValidVersion(reqRel, reqMaj, reqMin, rel, maj, min);
	}

	private boolean isValidVersion(final int reqRel, final int reqMaj, final int reqMin, final int actRel, final int actMaj, final int actMin) {
		return ((actRel > reqRel) || (actRel == reqRel && actMaj > reqMaj) || (actRel == reqRel && actMaj == reqMaj && actMin >= reqMin));
	}

	private boolean areAllGivenModuleInstalled(final List<String> modules) throws IOException {
		StringBuilder imports = new StringBuilder();
		for (String module : modules) {
			if (!imports.toString().isEmpty()) {
				imports.append(";");
			}
			imports.append(PY_IMPORT + module);
		}
		return !this.executeScript(imports.toString()).contains("ModuleNotFoundError");
	}

	private boolean isSingleModuleInstalled(final String moduleName) throws IOException {
		return !this.executeScript(PY_IMPORT + moduleName).contains("ModuleNotFoundError");
	}

	public List<String> getMissingModules(final String... modules) throws IOException {
		return this.getMissingModules(Arrays.asList(modules));
	}

	public List<String> getMissingModules(final List<String> modules) throws IOException {
		if (this.areAllGivenModuleInstalled(modules)) { // first try with a single invocation whether everything required is there.
			return Arrays.asList();
		}
		List<String> missingModules = new ArrayList<>(); // not realized via streams, because a stream would break the thrown exception!
		for (String m : modules) {
			if (!this.isSingleModuleInstalled(m)) {
				missingModules.add(m);
			}
		}
		return missingModules;
	}

	public boolean isModuleInstalled(final String... modules) throws IOException {
		return this.getMissingModules(modules).isEmpty();
	}
}
