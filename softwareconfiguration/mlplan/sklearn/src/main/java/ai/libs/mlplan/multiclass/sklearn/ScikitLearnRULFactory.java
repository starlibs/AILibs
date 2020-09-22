package ai.libs.mlplan.multiclass.sklearn;

import java.util.Set;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnRULFactory extends AScikitLearnLearnerFactory {

	public ScikitLearnRULFactory() {
		super(EScikitLearnProblemType.RUL);
	}

	@Override
	public String getPipelineBuildString(final IComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("timeseries_transformer"), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("data_cleaner"), importSet));
		sb.append(",");
		int i = 0;
		while (groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i) != null) {
			if (!groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i).getComponent().getName().equals("NoPreprocessor")) {
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i), importSet));
				sb.append(",");
			}
			i++;
		}
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("regressor"), importSet));
		return sb.toString();
	}
}
