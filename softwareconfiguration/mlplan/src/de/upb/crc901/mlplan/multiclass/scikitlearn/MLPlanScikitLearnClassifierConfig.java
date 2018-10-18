package de.upb.crc901.mlplan.multiclass.scikitlearn;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.mlplan.AbstractMLPlanConfig;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:conf/hasco/hasco.properties", "file:conf/mlplan/scikitlearn.properties" })
public interface MLPlanScikitLearnClassifierConfig extends AbstractMLPlanConfig {

}
