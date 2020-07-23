package ai.libs.mlplan.multiclass.sklearn;

import java.util.Set;

import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public abstract class ATwoStepPipelineScikitLearnFactory extends AScikitLearnLearnerFactory {

	private final String learnerFieldName;

	protected ATwoStepPipelineScikitLearnFactory(final EScikitLearnProblemType problemType, final String learnerFieldName) {
		super(problemType);
		this.learnerFieldName = learnerFieldName;
	}

	@Override
	public String getPipelineBuildString(final ComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR), importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(this.learnerFieldName), importSet));
		return sb.toString();
	}

}
