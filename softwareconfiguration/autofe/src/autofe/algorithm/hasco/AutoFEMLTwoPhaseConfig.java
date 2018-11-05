package autofe.algorithm.hasco;

import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;

@Sources({ "file:conf/autofeml-twophase.properties" })
public interface AutoFEMLTwoPhaseConfig extends MLPlanClassifierConfig, HASCOFeatureEngineeringConfig {

}
