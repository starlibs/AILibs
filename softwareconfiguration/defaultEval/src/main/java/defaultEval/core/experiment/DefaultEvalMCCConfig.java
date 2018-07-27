package defaultEval.core.experiment;

import java.io.File;
import java.util.List;

import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;

/**
 * 
 * @author fmohr
 *
 */
@Sources({ "file:../../../setup.properties" })
public interface DefaultEvalMCCConfig extends IExperimentSetConfig {
	public static final String DATASETS = "datasets";
	public static final String Classifiers = "classifiers";
	public static final String SEARCHERS = "searchers";
	public static final String EVALUATORS = "evaluators";
	public static final String OPTIMIZERS = "optimizers";
	public static final String SEEDS = "seeds";
	public static final String datasetFolder = "datasetfolder";
	public static final String environment = "environment";


	@Key(DATASETS)
	public List<String> getDatasets();
	
	@Key(Classifiers)
	public List<String> getClassifiers();
	
	@Key(SEARCHERS)
	public List<String> getSeachers();
	
	@Key(EVALUATORS)
	public List<String> getEvaluators();
	
	@Key(OPTIMIZERS)
	public List<String> getOptimizers();
	
	@Key(SEEDS)
	public List<String> getSeeds();
	
	@Key(datasetFolder)
	public File getDatasetFolder();
	
	@Key(environment)
	public File getEnvironment();
	
	
	
}
