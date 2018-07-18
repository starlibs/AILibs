package autofe.experiments;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;

@Sources({ "file:./conf/rankingExpConf.txt" })
public interface IRankingConfig extends IExperimentSetConfig {
	public static final String DATASETS = "datasets";
	public static final String BENCHMARKS = "benchmarks";
	public static final String TIMEOUTS_IN_SECONDS = "timeouts";

	@Key(DATASETS)
	public List<String> getDatasets();

	@Key(BENCHMARKS)
	public List<String> getBenchmarks();

	@Key(TIMEOUTS_IN_SECONDS)
	public List<String> getTimeouts();
}
