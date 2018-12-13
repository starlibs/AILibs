package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.IOException;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;

public class WekaMLPlanWekaClassifier extends MLPlanWekaClassifier {
	
	public WekaMLPlanWekaClassifier(MLPlanBuilder builder) throws IOException {
		super(builder);
		builder.withAutoWEKAConfiguration();
	}
	
	public WekaMLPlanWekaClassifier() throws IOException {
		this(new MLPlanBuilder());

	}
}
