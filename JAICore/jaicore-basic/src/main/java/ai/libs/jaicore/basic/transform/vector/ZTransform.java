package ai.libs.jaicore.basic.transform.vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.StatisticsUtil;

public class ZTransform implements IVectorTransform {

	public static final double EPSILON = 0.0000001;

	@Override
	public double[] transform(final double[] input) {
		List<Double> t = Arrays.stream(input).mapToObj(Double::valueOf).collect(Collectors.toList());
		double mean = StatisticsUtil.mean(t);
		double standardDeviation = StatisticsUtil.standardDeviation(t);
		if ((-EPSILON < standardDeviation) && (standardDeviation < EPSILON)) {
			return new double[input.length]; // All zeros.
		}
		double[] zTransformedT = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			zTransformedT[i] = (input[i] - mean) / standardDeviation;
		}
		return zTransformedT;
	}

}
