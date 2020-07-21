package ai.libs.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.basic.SystemRequirementsNotMetException;

public class PythonUtil {
	private static String CMD_PYTHON;
	private static final String CMD_PYTHON_COMMANDPARAM = "-c";

	private static final String PY_IMPORT = "import ";

	private final File pathToPathonExecutable;

	public PythonUtil() {
		this(ConfigCache.getOrCreate(IPythonConfig.class));
	}

	public PythonUtil(final IPythonConfig config) {
		this(config.getPath());
	}

	public PythonUtil(final String pathToFolderContainingThePythonExecutable) {
		this(pathToFolderContainingThePythonExecutable != null ? new File(pathToFolderContainingThePythonExecutable) : null);
	}

	public PythonUtil(final File pathToFolderContainingThePythonExecutable) {
		super();
		if (pathToFolderContainingThePythonExecutable != null) {
			if (!pathToFolderContainingThePythonExecutable.exists()) {
				throw new IllegalArgumentException("The path to python executable " + pathToFolderContainingThePythonExecutable.getAbsolutePath() + " does not exist.");
			}
			if (!new File(pathToFolderContainingThePythonExecutable + File.separator + CMD_PYTHON).exists()) {
				throw new IllegalArgumentException("The given path does not contain an executable with name " + CMD_PYTHON);
			}
		}
		this.pathToPathonExecutable = pathToFolderContainingThePythonExecutable;
		IPythonConfig config = ConfigCache.getOrCreate(IPythonConfig.class);
		CMD_PYTHON = config.getPythonCommand();

	}

	public ProcessBuilder getProcessBuilder() {
		return new ProcessBuilder();
	}

	public String executeScript(final String script) throws IOException {
		ProcessBuilder processBuilder = this.getProcessBuilder();
		processBuilder.redirectErrorStream(true);
		String command = (this.pathToPathonExecutable != null ? this.pathToPathonExecutable.getAbsolutePath() + File.separator : "") + CMD_PYTHON;
		Process p = processBuilder.command(command, CMD_PYTHON_COMMANDPARAM, script).start();
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
		return this.executeScript(PY_IMPORT + "platform\nprint(platform.python_version())");
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
