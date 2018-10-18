package de.upb.crc901.mlplan.multiclass.weka;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.mlplan.AbstractMLPlanConfig;

@Sources({ "file:conf/mlplan-weka.properties" })
public interface MLPlanWekaClassifierConfig extends AbstractMLPlanConfig {

	@Override
	@DefaultValue("model/weka/weka-all-autoweka.json")
	public File componentsFile();
}
