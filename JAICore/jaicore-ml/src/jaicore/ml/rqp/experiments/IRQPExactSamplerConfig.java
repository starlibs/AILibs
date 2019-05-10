package jaicore.ml.rqp.experiments;

import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;
import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/rqp_exact_samplerV2.properties" })
public interface IRQPExactSamplerConfig extends IMultiClassClassificationExperimentConfig {

}
