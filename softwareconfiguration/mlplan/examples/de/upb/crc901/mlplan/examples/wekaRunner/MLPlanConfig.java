package de.upb.crc901.mlplan.examples.wekaRunner;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/mlplan.properties" })
public interface MLPlanConfig extends IMultiClassClassificationExperimentConfig {

	public static final String K_MLPLAN_TIMEOUT = "mlplan.globaltimeout";
	public static final String K_MLPLAN_EVAL_TIMEOUT = "mlplan.evaltimeout";
	public static final String K_MLPLAN_DATASET_FILE = "mlplan.datasetfile";
	public static final String K_MLPLAN_OUTFILE = "mlplan.outputfile";

	@Key(K_MLPLAN_TIMEOUT)
	public int getTimeout();

	@Key(K_MLPLAN_EVAL_TIMEOUT)
	public int getEvalTimeout();

	@Key(K_MLPLAN_DATASET_FILE)
	public String getDatasetFile();

	@Key(K_MLPLAN_OUTFILE)
	public File getOutputFile();

}
