package jaicore.ml.tsc.classifier;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/classifier-eval.properties" })
public interface TSClassifierExperimentConfig extends IMultiClassClassificationExperimentConfig {
}
