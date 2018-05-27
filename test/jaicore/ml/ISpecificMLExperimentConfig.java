package jaicore.ml;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/mlexperiments.properties" })
public interface ISpecificMLExperimentConfig extends IMultiClassClassificationExperimentConfig {
	public static final String datasetFolder = "datasetfolder";
	
	@Key(datasetFolder)
	public String getDatasetFolder();
}
