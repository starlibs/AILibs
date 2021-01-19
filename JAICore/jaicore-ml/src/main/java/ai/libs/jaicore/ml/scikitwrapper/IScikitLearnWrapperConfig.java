package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import ai.libs.python.IPythonConfig;

@Sources({ "file:conf/scikitlearn_wrapper.properties" })
public interface IScikitLearnWrapperConfig extends IPythonConfig {

	public static final String K_TEMP_FOLDER = "sklearn.wrapper.temp.folder";

	public static final String DEF_TEMP_FOLDER = "tmp";

	@Key("sklearn.wrapper.python.extension")
	@DefaultValue(".py")
	public String getPythonFileExtension();

	@Key("sklearn.wrapper.pickle.extension")
	@DefaultValue(".pcl")
	public String getPickleFileExtension();

	@Key("sklearn.wrapper.result.extension")
	@DefaultValue(".json")
	public String getResultFileExtension();

	@Key("sklearn.wrapper.temp.delete_on_exit")
	@DefaultValue("true")
	public boolean getDeleteFileOnExit();

	@Key(K_TEMP_FOLDER)
	@DefaultValue(DEF_TEMP_FOLDER)
	public File getTempFolder();

	@Key("sklearn.wrapper.temp.dump_folder_name")
	@DefaultValue("model_dumps")
	public String getModelDumpsDirectoryName();

	default File getModelDumpsDirectory() {
		return new File(this.getTempFolder(), this.getModelDumpsDirectoryName());
	}

}
