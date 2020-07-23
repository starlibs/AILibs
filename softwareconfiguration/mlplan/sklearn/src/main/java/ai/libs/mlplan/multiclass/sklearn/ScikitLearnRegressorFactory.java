package ai.libs.mlplan.multiclass.sklearn;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnRegressorFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnRegressorFactory() {
		super(EScikitLearnProblemType.REGRESSION, "regressor");
	}

}
