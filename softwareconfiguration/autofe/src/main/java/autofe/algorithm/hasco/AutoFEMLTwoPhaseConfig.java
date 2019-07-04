package autofe.algorithm.hasco;

import org.aeonbits.owner.Config.Sources;

import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

@Sources({ "file:conf/autofeml-twophase.properties" })
public interface AutoFEMLTwoPhaseConfig extends MLPlanClassifierConfig, HASCOFeatureEngineeringConfig {

}
