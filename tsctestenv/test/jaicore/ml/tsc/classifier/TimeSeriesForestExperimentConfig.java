package jaicore.ml.tsc.classifier;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/tsf-eval.properties" })
public interface TimeSeriesForestExperimentConfig extends IMultiClassClassificationExperimentConfig {
	/**
	 * Time series forest classifier parameters
	 */
	public static final String TSF_NUM_TREES = "tsf.numTrees";
	public static final String TSF_MAX_DEPTH = "tsf.maxDepths";

	@Key(TSF_NUM_TREES)
	public List<String> getTSFNumTrees();

	@Key(TSF_MAX_DEPTH)
	public List<String> getTSFMaxDepth();
}
