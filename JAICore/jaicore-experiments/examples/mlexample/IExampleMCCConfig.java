package mlexample;

import java.io.File;
import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;

/**
 * In fact, this config is an exact copy of IMultiClassClassificationExperimentConfig,
 * which cannot be used here to avoid cyclic dependencies.
 * Typically, in this case, you just would extend IMultiClassClassificationExperimentConfig
 * and leave the interface (mostly) empty except that you define the URL for the file.
 * 
 * 
 * @author fmohr
 *
 */
@Sources({ "file:./examples/mlexample/setup.properties" })
public interface IExampleMCCConfig extends IExperimentSetConfig {
	public static final String DATASETS = "datasets";
	public static final String ALGORITHMS = "algorithms";
	public static final String ALGORITHMMODES = "algorithmmodes";
	public static final String SEEDS = "seeds";
	public static final String TIMEOUTS_IN_SECONDS = "timeouts";
	public static final String MEASURES = "measures";
	public static final String datasetFolder = "datasetfolder";
	
	@Key(DATASETS)
	public List<String> getDatasets();
	
	@Key(ALGORITHMS)
	public List<String> getAlgorithms();
	
	@Key(ALGORITHMMODES)
	public List<String> getAlgorithmModes();
	
	@Key(SEEDS)
	public List<String> getSeeds();
	
	@Key(TIMEOUTS_IN_SECONDS)
	public List<String> getTimeouts();
	
	@Key(MEASURES)
	public List<String> getMeasures();
	
	@Key(datasetFolder)
	public File getDatasetFolder();
}
