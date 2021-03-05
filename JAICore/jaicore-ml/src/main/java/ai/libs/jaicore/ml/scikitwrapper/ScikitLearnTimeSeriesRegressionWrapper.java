package ai.libs.jaicore.ml.scikitwrapper;

import java.io.IOException;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnTimeSeriesRegressionWrapper<P extends IPrediction, B extends IPredictionBatch> extends ScikitLearnRegressionWrapper<P, B> {

	public ScikitLearnTimeSeriesRegressionWrapper(final String pipeline, final String imports) throws IOException {
		super(EScikitLearnProblemType.TIME_SERIES_REGRESSION, pipeline, imports);
	}

}
