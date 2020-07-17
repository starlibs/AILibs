package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;

public abstract class ARegressionLossTest {

	protected ARegressionLossTest() {
		// TODO Auto-generated constructor stub
	}

	protected List<IRegressionPrediction> toPredictions(final List<Double> values) {
		return values.stream().map(SingleTargetRegressionPrediction::new).collect(Collectors.toList());
	}

}
