package defaultEval.core.experiment;

import java.io.File;
import java.util.List;

import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;

/**
 * 
 * @author jnowack
 *
 */
@Sources({ "file:../../../setup.properties" })
public interface DefaultEvalMCCConfig extends IExperimentSetConfig {
	public static final String DATASETS = "datasets";
	public static final String Classifiers = "classifiers";
	public static final String PREPROCESSORS = "preprocessors";
	public static final String OPTIMIZERS = "optimizers";
	public static final String SEEDS = "seeds";
	public static final String datasetFolder = "datasetfolder";
	public static final String environment = "environment";
	public static final String max_runtime_param = "max_runtime_param";
	public static final String max_runtime = "max_runtime";
	
	@Key(DATASETS)
	public List<String> getDatasets();
	
	@Key(Classifiers)
	public List<String> getClassifiers();
	
	@Key(PREPROCESSORS)
	public List<String> getPreprocessors();
	
	@Key(OPTIMIZERS)
	public List<String> getOptimizers();
	
	@Key(SEEDS)
	public List<String> getSeeds();
	
	@Key(datasetFolder)
	public File getDatasetFolder();
	
	@Key(environment)
	public File getEnvironment();
	
	@Key(max_runtime_param)
	public int getMaxRuntimeParam();
	
	@Key(max_runtime)
	public int getMaxRuntime();
	
}
