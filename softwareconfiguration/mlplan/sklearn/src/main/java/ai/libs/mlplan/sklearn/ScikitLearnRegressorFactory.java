package ai.libs.mlplan.sklearn;

import java.io.IOException;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.scikitwrapper.AScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnRegressionWrapper;

public class ScikitLearnRegressorFactory extends ATwoStepPipelineScikitLearnFactory {

	public ScikitLearnRegressorFactory() {
		super("regressor");
	}

	@Override
	public AScikitLearnWrapper<IPrediction, IPredictionBatch> getScikitLearnWrapper(final String constructionString, final String imports) throws IOException {
		return new ScikitLearnRegressionWrapper<>(constructionString, imports);
	}

}
