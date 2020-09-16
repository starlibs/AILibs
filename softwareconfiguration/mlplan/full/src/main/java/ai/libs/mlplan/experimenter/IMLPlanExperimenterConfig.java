package ai.libs.mlplan.experimenter;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.IExperimentSetConfig;

@Sources({ "file:./experiment-setup.properties" })
public interface IMLPlanExperimenterConfig extends IExperimentSetConfig, IDatabaseConfig {

}
