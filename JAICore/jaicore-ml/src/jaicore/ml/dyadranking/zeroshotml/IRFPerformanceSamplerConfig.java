package jaicore.ml.dyadranking.zeroshotml;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/perfsampler/rf_sampler.properties" })
public interface IRFPerformanceSamplerConfig extends IMultiClassClassificationExperimentConfig {

}
