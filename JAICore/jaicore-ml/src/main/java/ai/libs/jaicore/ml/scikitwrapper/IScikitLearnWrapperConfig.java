package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;

import ai.libs.python.IPythonConfig;

public interface IScikitLearnWrapperConfig extends IPythonConfig {

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
	@DefaultValue("false")
	public boolean getDeleteFileOnExit();

	@Key("sklearn.wrapper.temp.folder")
	@DefaultValue(DEF_TEMP_FOLDER)
	public File getTempFolder();

	@Key("sklearn.wrapper.temp.dump_folder")
	@DefaultValue(DEF_TEMP_FOLDER + "/model_dumps")
	public File getModelDumpsDirectory();

}
