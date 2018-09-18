package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/mlplanwekacli.properties" })
public interface MLPlanWekaCLIConfig extends IMultiClassClassificationExperimentConfig {

	public static final String K_MLPLAN_TIMEOUT = "mlplanwekacli.globaltimeout";
	public static final String K_MLPLAN_EVAL_TIMEOUT = "mlplanwekacli.evaltimeout";
	public static final String K_MLPLAN_DATASET_FILE = "mlplanwekacli.datasetfile";
	public static final String K_MLPLAN_OUTFILE = "mlplanwekacli.outputfile";

	public static final String K_SHOW_GRAPH_VISUALIZATION = "mlplanwekacli.showgrpah";

	@Key(K_MLPLAN_TIMEOUT)
	public int timeout();

	@Key(K_MLPLAN_EVAL_TIMEOUT)
	public int evalTimeout();

	@Key(K_MLPLAN_DATASET_FILE)
	public String datasetFile();

	@Key(K_MLPLAN_OUTFILE)
	@DefaultValue("out.txt")
	public File outputFile();

	@Key(K_SHOW_GRAPH_VISUALIZATION)
	@DefaultValue("true")
	public boolean showGraphVisualization();

}
