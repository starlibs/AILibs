package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AMultiLabelClassificationMeasure;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.IOWAValueFunction;

public class OWARelevanceLoss extends AMultiLabelClassificationMeasure {

	private final IOWAValueFunction valueFunction;

	public OWARelevanceLoss(final IOWAValueFunction valueFunction) {
		this.valueFunction = valueFunction;
	}

	/**
	 * Function f to be integrated: f(c_i) = u_i = 1 - | s_i - y_i |
	 * s_i \in [0,1]: label relevance score predicted
	 * y_i \in {0,1}: ground truth
	 *
	 * @param expected
	 * @param actual
	 * @return
	 */
	private double fcrit(final int expected, final double actual) {
		return 1 - Math.abs(actual - expected);
	}

	private double instanceLoss(final int[] expected, final double[] actual) {
		double sum = 0.0;
		double m = expected.length;
		List<Double> listOfCis = IntStream.range(0, expected.length).mapToObj(i -> this.fcrit(expected[i], actual[i])).collect(Collectors.toList());
		listOfCis.add(0.0);
		Collections.sort(listOfCis);
		for (int i = 1; i < listOfCis.size(); i++) {
			sum += (this.valueFunction.transform((m - i + 1), m) - this.valueFunction.transform((m - i), m)) * listOfCis.get(i);
		}
		return 1 - sum;
	}

	@Override
	public double loss(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> actual) {
		this.checkConsistency(expected, actual);
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < expected.size(); i++) {
			stats.addValue(this.instanceLoss(expected.get(i), actual.get(i).getPrediction()));
		}
		return stats.getMean();
	}

	@Override
	public double score(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> actual) {
		return 1 - this.loss(expected, actual);
	}
}
