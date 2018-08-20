package jaicore.ml.rqp.experiments;

import java.io.File;

import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Sources;

import jaicore.experiments.IExperimentSetConfig;
import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/rqp_aug_space_samplers.properties" })
public interface IRQPAugSpaceSamplerConfig extends IMultiClassClassificationExperimentConfig {

}
