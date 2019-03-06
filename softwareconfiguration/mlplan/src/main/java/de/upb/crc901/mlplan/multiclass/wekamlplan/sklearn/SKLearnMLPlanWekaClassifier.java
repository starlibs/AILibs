package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;

public class SKLearnMLPlanWekaClassifier extends MLPlanWekaClassifier {

	public SKLearnMLPlanWekaClassifier(final MLPlanBuilder builder) throws IOException {
		super(builder);
	}

	public SKLearnMLPlanWekaClassifier() throws IOException {
		super(new MLPlanBuilder().withAutoSKLearnConfig());
	}

}
