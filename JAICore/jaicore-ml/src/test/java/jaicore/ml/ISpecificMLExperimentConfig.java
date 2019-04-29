package jaicore.ml;

import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IDatabaseConfig;
import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/mlexperiments.properties" })
public interface ISpecificMLExperimentConfig extends IMultiClassClassificationExperimentConfig, IDatabaseConfig {
	
}
