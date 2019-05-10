package ai.libs.mlplan.examples.multiclass.weka;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.experiments.IDatabaseConfig;
import ai.libs.jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/mlplan-weka-eval.properties" })
public interface MLPlanWekaExperimenterConfig extends IMultiClassClassificationExperimentConfig, IDatabaseConfig {

	public static final String DB_EVAL_TABLE = "db.evalTable";

	@Key(DB_EVAL_TABLE)
	@DefaultValue("evaluations_mls")
	public String evaluationsTable();

}
