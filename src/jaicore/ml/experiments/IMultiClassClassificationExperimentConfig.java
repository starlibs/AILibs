package jaicore.ml.experiments;

import java.util.List;

import jaicore.experiments.IExperimentConfig;

public interface IMultiClassClassificationExperimentConfig extends IExperimentConfig {
	public static final String DATASETS = "datasets";
	public static final String ALGORITHMS = "algorithms";
	public static final String ALGORITHMMODES = "algorithmmodes";
	public static final String SEEDS = "seeds";
	public static final String TIMEOUTS_IN_SECONDS = "timeouts";
	public static final String MEASURES = "measures";
	
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
}
