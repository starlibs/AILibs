package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class WeightedAbsoluteError extends ARegressionMeasure {

	private double weightA = 1d;
	
	
	public WeightedAbsoluteError(double weightA) {
		this.weightA = weightA;
	}


	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double d = expected.get(i) - actual.get(i);
			Double error = 0d;
			if(d<=0) {
				error = -weightA*d;
			}else {
				error = weightA*d;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}