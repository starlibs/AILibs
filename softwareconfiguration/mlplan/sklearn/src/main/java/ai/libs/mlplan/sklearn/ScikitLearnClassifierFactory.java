package ai.libs.mlplan.sklearn;

import java.io.IOException;

import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.simple.SimpleScikitLearnClassifier;

public class ScikitLearnClassifierFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnClassifierFactory() {
		super("learner");
	}

	@Override
	public IScikitLearnWrapper getScikitLearnWrapper(final String constructionString, final String imports) throws IOException, InterruptedException {
		return new SimpleScikitLearnClassifier(constructionString, imports);
	}

}
