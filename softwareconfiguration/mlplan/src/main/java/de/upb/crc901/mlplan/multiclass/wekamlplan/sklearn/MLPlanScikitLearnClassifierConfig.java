package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:conf/hasco/hasco.properties", "file:conf/mlplan/scikitlearn.properties" })
public interface MLPlanScikitLearnClassifierConfig extends TwoPhaseHASCOConfig {

}
