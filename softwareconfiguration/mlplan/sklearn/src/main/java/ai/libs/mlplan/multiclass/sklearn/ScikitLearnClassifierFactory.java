package ai.libs.mlplan.multiclass.sklearn;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnClassifierFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnClassifierFactory() {
		super(EScikitLearnProblemType.CLASSIFICATION, "classifier");
	}

}
