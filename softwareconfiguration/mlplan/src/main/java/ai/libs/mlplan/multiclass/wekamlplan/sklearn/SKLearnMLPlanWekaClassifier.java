package ai.libs.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;

import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;

public class SKLearnMLPlanWekaClassifier extends MLPlanWekaClassifier {

	public SKLearnMLPlanWekaClassifier(final AbstractMLPlanBuilder builder) throws IOException {
		super(builder);
	}

	public SKLearnMLPlanWekaClassifier() throws IOException {
		super(AbstractMLPlanBuilder.forSKLearn());
	}

}
