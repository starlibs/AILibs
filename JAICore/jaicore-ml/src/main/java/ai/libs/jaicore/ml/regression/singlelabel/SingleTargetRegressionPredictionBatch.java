package ai.libs.jaicore.ml.regression.singlelabel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;

public class SingleTargetRegressionPredictionBatch extends ArrayList<IRegressionPrediction> implements IRegressionResultBatch {

	private static final long serialVersionUID = 1L;

	public SingleTargetRegressionPredictionBatch(final Collection<IRegressionPrediction> predictions) {
		this.addAll(predictions);
	}

	@Override
	public int getNumPredictions() {
		return this.size();
	}

	@Override
	public List<? extends IRegressionPrediction> getPredictions() {
		return this;
	}

}
