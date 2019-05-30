package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;

public class SKLearnMLPlanWekaClassifier extends MLPlanWekaClassifier {

	public SKLearnMLPlanWekaClassifier(final AbstractMLPlanBuilder builder) throws IOException {
		super(builder);
	}

	public SKLearnMLPlanWekaClassifier() throws IOException {
		super(AbstractMLPlanBuilder.forSKLearn());
	}

}
