package ai.libs.jaicore.ml;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.ml.core.evaluation.experiment.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/mlexperiments.properties" })
public interface ISpecificMLExperimentConfig extends IMultiClassClassificationExperimentConfig, IDatabaseConfig {
	
}
