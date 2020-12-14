package ai.libs.mlplan.sklearn;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnRegressorFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnRegressorFactory() {
		super(EScikitLearnProblemType.REGRESSION, "regressor");
	}

}
