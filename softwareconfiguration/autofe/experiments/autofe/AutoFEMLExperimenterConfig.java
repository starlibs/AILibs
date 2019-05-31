package autofe;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/autofeml-eval.properties" })
public interface AutoFEMLExperimenterConfig extends IMultiClassClassificationExperimentConfig {

	@Key("db.evalTable")
	@DefaultValue("dev_eval")
	public String evalTable();

	@Key("gui.enablevisualization")
	@DefaultValue("false")
	public boolean enableVisualization();

}
