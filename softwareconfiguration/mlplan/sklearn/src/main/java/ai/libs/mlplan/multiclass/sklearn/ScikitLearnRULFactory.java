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
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("timeseries_transformer").iterator().next(), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("data_cleaner").iterator().next(), importSet));
		sb.append(",");
		int i = 0;
		while (groundComponent.getSatisfactionOfRequiredInterface(N_PREPROCESSOR + i) != null) {
			if (!groundComponent.getSatisfactionOfRequiredInterface(N_PREPROCESSOR + i).iterator().next().getComponent().getName().equals("NoPreprocessor")) {
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface(N_PREPROCESSOR + i).iterator().next(), importSet));
				sb.append(",");
			}
			i++;
		}
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("regressor").iterator().next(), importSet));
		return sb.toString();
	}
}
