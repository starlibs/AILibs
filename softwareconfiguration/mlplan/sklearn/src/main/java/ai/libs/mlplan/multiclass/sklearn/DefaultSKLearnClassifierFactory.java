package ai.libs.mlplan.multiclass.sklearn;

import java.util.Set;

import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;

public class DefaultSKLearnClassifierFactory  extends ASKLearnClassifierFactory<SingleLabelClassification, SingleLabelClassificationPredictionBatch> {

	@Override
	public String getPipelineBuildString(final ComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier"), importSet));
		return sb.toString();
	}

}
