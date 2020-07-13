package ai.libs.mlplan.multiclass.sklearn;

import java.io.IOException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;

public class MLPlanSKLearnStandardBuilder extends MLPlanSKLearnBuilder<SingleLabelClassification, SingleLabelClassificationPredictionBatch> {

	public MLPlanSKLearnStandardBuilder() throws IOException {
		super();
	}

}
