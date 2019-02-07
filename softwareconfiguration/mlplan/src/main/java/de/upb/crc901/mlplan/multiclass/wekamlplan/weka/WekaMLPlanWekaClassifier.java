package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.IOException;

import de.upb.crc901.mlplan.multiclass.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;

public class WekaMLPlanWekaClassifier extends MLPlanWekaClassifier {
	
	public WekaMLPlanWekaClassifier(MLPlanBuilder builder) throws IOException {
		super(builder);
		builder.withAutoWEKAConfiguration();
		PreferenceBasedNodeEvaluator preferenceNodeEvaluator = new PreferenceBasedNodeEvaluator(new ComponentLoader(getComponentFile()).getComponents(), FileUtil.readFileAsList(getMLPlanConfig().preferredComponents()));
		this.setPreferredNodeEvaluator(preferenceNodeEvaluator);
	}
	
	public WekaMLPlanWekaClassifier() throws IOException {
		this(new MLPlanBuilder());

	}
}
