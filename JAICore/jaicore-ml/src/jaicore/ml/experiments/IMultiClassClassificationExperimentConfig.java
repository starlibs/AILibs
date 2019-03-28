package jaicore.ml.experiments;

import java.io.File;
import java.util.List;

import jaicore.experiments.IExperimentSetConfig;

public interface IMultiClassClassificationExperimentConfig extends IExperimentSetConfig {
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
