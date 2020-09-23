package ai.libs.mlplan.multiclass.sklearn;

import java.util.Set;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public abstract class ATwoStepPipelineScikitLearnFactory extends AScikitLearnLearnerFactory {

	private final String learnerFieldName;

	protected ATwoStepPipelineScikitLearnFactory(final EScikitLearnProblemType problemType, final String learnerFieldName) {
		super(problemType);
		this.learnerFieldName = learnerFieldName;
	}

	@Override
	public String getPipelineBuildString(final IComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface(N_PREPROCESSOR).iterator().next(), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface(this.learnerFieldName).iterator().next(), importSet));
		return sb.toString();
	}

}
