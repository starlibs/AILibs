package ai.libs.mlplan.multiclass.sklearn;

import java.util.Set;

import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnRULFactory extends AScikitLearnLearnerFactory {

	public ScikitLearnRULFactory() {
		super(EScikitLearnProblemType.RUL);
	}

	@Override
	public String getPipelineBuildString(final ComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("timeseries_transformer").get(0), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("data_cleaner").get(0), importSet));
		sb.append(",");
		int i = 0;
		while (groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i) != null) {
			if (!groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i).get(0).getComponent().getName().equals("NoPreprocessor")) {
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i).get(0), importSet));
				sb.append(",");
			}
			i++;
		}
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("regressor").get(0), importSet));
		return sb.toString();
	}
}
