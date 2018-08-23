package de.upb.crc901.mlplan.examples.multiclass.weka;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/mlplan-weka-eval.properties" })
public interface MLPlanWekaExperimenterConfig extends IMultiClassClassificationExperimentConfig {

}
