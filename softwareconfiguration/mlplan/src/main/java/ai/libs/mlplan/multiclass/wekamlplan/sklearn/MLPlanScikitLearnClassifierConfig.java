package ai.libs.mlplan.multiclass.wekamlplan.sklearn;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:conf/hasco/hasco.properties", "file:conf/mlplan/scikitlearn.properties" })
public interface MLPlanScikitLearnClassifierConfig extends TwoPhaseHASCOConfig {

}
