package jaicore.ml.tsc.classifier;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/lps-eval.properties" })
public interface LearnPatternSimilarityExperimentConfig extends IMultiClassClassificationExperimentConfig {
	/**
	 * Learn Pattern Similarity classifier parameters
	 */
	public static final String LPS_NUM_TREES = "lps.numTrees";
	public static final String LPS_MAX_TREE_DEPTH = "lps.maxTreeDepths";
	public static final String LPS_NUM_SEGMENTS = "lps.numSegments";

	@Key(LPS_NUM_TREES)
	public List<String> getNumTrees();

	@Key(LPS_MAX_TREE_DEPTH)
	public List<String> getMaxTreeDepths();

	@Key(LPS_NUM_SEGMENTS)
	public List<String> getNumSegments();
}
