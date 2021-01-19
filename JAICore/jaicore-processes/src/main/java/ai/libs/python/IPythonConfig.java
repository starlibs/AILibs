package ai.libs.python;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.basic.IOwnerBasedConfig;

@Sources({ "file:conf/python.properties" })
public interface IPythonConfig extends IOwnerBasedConfig {

	public static final String KEY_PATH_TO_PYTHON_EXECUTABLE = "pathToPythonExecutable";
	public static final String KEY_PYTHON = "pythonCmd";
	public static final String KEY_ANACONDA = "anaconda";
	public static final String KEY_PATH_TO_ANACONDA_EXECUTABLE = "pathToCondaExecutable";

	@Key(KEY_PATH_TO_PYTHON_EXECUTABLE)
	public String getPathToPythonExecutable();

	@Key(KEY_PYTHON)
	@DefaultValue("python3")
	public String getPythonCommand();

	@Key(KEY_ANACONDA)
	public String getAnacondaEnvironment();

	@Key(KEY_PATH_TO_ANACONDA_EXECUTABLE)
	@DefaultValue("~/anaconda3/etc/profile.d/conda.sh")
	public String getPathToAnacondaExecutable();

}
