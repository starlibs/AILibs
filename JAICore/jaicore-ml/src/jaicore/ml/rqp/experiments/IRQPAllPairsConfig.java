package jaicore.ml.rqp.experiments;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:./conf/rqp_allpairs.properties" })
public interface IRQPAllPairsConfig extends IMultiClassClassificationExperimentConfig {

}
