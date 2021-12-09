package ai.libs.mlplan.sklearn;

import java.io.IOException;

import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.simple.SimpleScikitLearnRegressor;

public class ScikitLearnRegressorFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnRegressorFactory() {
		super("learner");
	}

	@Override
	public IScikitLearnWrapper getScikitLearnWrapper(final String constructionString, final String imports) throws IOException, InterruptedException {
		return new SimpleScikitLearnRegressor(constructionString, imports);
	}

}
