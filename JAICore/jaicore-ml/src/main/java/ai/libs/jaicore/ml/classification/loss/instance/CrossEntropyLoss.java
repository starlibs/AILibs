package ai.libs.jaicore.ml.classification.loss.instance;

import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class CrossEntropyLoss extends AInstanceMeasure<double[], ISingleLabelClassification> {

	public static final double DEF_EPSILON = 1E-15;

	private final double epsilon;

	public CrossEntropyLoss() {
		this(DEF_EPSILON);
	}

	public CrossEntropyLoss(final double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public double loss(final double[] expected, final ISingleLabelClassification predicted) {
		Map<Integer, Double> distributionMap = predicted.getClassDistribution();
		double[] predictedArr = new double[distributionMap.size()];
		IntStream.range(0, distributionMap.size()).forEach(x -> predictedArr[x] = distributionMap.get(x));
		return -IntStream.range(0, expected.length).mapToDouble(i -> expected[i] * Math.log(this.minMax(predictedArr[i]))).sum();
	}

	private double minMax(final double value) {
		return Math.min(1 - this.epsilon, Math.max(value, this.epsilon));
	}

}
