package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AMultiLabelClassificationMeasure;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.choquistic.IMassFunction;

public class ChoquisticRelevanceLoss extends AMultiLabelClassificationMeasure {

	private final IMassFunction measure;

	public ChoquisticRelevanceLoss(final IMassFunction measure) {
		super();
		this.measure = measure;
	}

	public ChoquisticRelevanceLoss(final double threshold, final IMassFunction measure) {
		super(threshold);
		this.measure = measure;
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
		List<Double> listOfCis = IntStream.range(0, expected.length).mapToObj(i -> this.fcrit(expected[i], actual[i])).collect(Collectors.toList());
		listOfCis.add(0.0);
		Collections.sort(listOfCis);
		for (int i = 1; i < listOfCis.size(); i++) {
			sum += (listOfCis.get(i) - listOfCis.get(i - 1)) * this.measure.mu(IntStream.range(i, listOfCis.size()).mapToObj(listOfCis::get).collect(Collectors.toList()), expected.length);
		}
		return sum;
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