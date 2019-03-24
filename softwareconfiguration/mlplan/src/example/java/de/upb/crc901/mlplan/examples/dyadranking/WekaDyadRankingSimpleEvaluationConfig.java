package de.upb.crc901.mlplan.examples.dyadranking;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/dyadranking/dyadmlplan.properties" })
public interface WekaDyadRankingSimpleEvaluationConfig extends IMultiClassClassificationExperimentConfig {

	public static final String DB_EVAL_TABLE = "db.evalTable";

	@Key(DB_EVAL_TABLE)
	@DefaultValue("partial_pipelines_simple_intermediate")
	public String evaluationsTable();
}
