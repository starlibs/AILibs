package ai.libs.jaicore.ml;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.experiments.IDatabaseConfig;
import ai.libs.jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/mlexperiments.properties" })
public interface ISpecificMLExperimentConfig extends IMultiClassClassificationExperimentConfig, IDatabaseConfig {
	
}
