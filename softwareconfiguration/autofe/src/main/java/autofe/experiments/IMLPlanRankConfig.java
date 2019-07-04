package autofe.experiments;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.experiments.IExperimentSetConfig;

@Sources({ "file:conf/mlplanRankConf.txt" })
public interface IMLPlanRankConfig extends IExperimentSetConfig {
	public static final String DATASETS = "datasets";
	public static final String VARIATIONS = "variations";
	public static final String TIMEOUTS_IN_SECONDS = "timeouts";
	public static final String SEEDS = "seeds";
	public static final String DATASET_FOLDER = "datasetfolder";

	@Key(DATASETS)
	public List<String> getDatasets();

	@Key(VARIATIONS)
	public List<String> getVariations();

	@Key(TIMEOUTS_IN_SECONDS)
	public List<String> getTimeouts();

	@Key(SEEDS)
	public List<String> getSeeds();

	@Key(DATASET_FOLDER)
	public String getDatasetFolder();
}
