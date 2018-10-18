package de.upb.crc901.mlplan.mlsplan.multiclass.combo;

import java.io.File;

import de.upb.crc901.mlplan.AbstractMLPlanConfig;

public interface MLSPlanCombiClassifierConfig extends AbstractMLPlanConfig {

	@Override
	@DefaultValue("model/combined/combinedML.json")
	public File componentsFile();
}
