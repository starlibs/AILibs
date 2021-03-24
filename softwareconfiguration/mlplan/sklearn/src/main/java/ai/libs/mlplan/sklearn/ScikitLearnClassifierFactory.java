package ai.libs.mlplan.sklearn;

import java.io.IOException;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.scikitwrapper.AScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnClassificationWrapper;

public class ScikitLearnClassifierFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnClassifierFactory() {
		super("classifier");
	}

	@Override
	public AScikitLearnWrapper<IPrediction, IPredictionBatch> getScikitLearnWrapper(final String constructionString, final String imports) throws IOException {
		return new ScikitLearnClassificationWrapper<>(constructionString, imports);
	}

}
