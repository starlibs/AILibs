package ai.libs.jaicore.basic.transform.vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.StatisticsUtil;

public class NormalizeByStdTransform implements IVectorTransform {

	@Override
	public double[] transform(final double[] input) {
		List<Double> t = Arrays.stream(input).mapToObj(Double::valueOf).collect(Collectors.toList());
		double standardDeviation = StatisticsUtil.standardDeviation(t);
		if (standardDeviation == 0) {
			return new double[input.length];
		}
		double[] normalizedT = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			normalizedT[i] = input[i] / standardDeviation;
		}
		return normalizedT;
	}

}
