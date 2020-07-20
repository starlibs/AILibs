package ai.libs.python;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.basic.IOwnerBasedConfig;

@Sources({ "file:conf/python.properties" })
public interface IPythonConfig extends IOwnerBasedConfig {

	public static final String KEY_PATH = "path";
	public static final String KEY_ANACONDA = "anaconda";

	@Key(KEY_PATH)
	public String getPath();

	@Key(KEY_ANACONDA)
	public String getAnacondaEnvironment();

}
