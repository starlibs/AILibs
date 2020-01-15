package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;

/**
 * Measure combining exact match, hamming loss, f1macroavgL and rankloss. Here
 * implemented in inverse.
 *
 * de S�, Alex GC, Gisele L. Pappa, and Alex A. Freitas. "Towards a method for automatically selecting and configuring multi-label
 * classification algorithms." Proceedings of the Genetic and Evolutionary Computation Conference Companion. ACM, 2017.
 *
 * @author mwever
 *
 */
public class AutoMEKAGGPFitnessMeasureLoss extends AThresholdBasedMultiLabelClassificationMeasure {

	private IMultiLabelClassificationPredictionPerformanceMeasure[] measures;

	@SuppressWarnings("unchecked")
	public AutoMEKAGGPFitnessMeasureLoss() {
		super();
		this.measures = new IMultiLabelClassificationPredictionPerformanceMeasure[] { new ExactMatch(), new F1MacroAverageL(), new Hamming(), new RankLoss() };
	}

	@SuppressWarnings("unchecked")
	public AutoMEKAGGPFitnessMeasureLoss(final double threshold) {
		super(threshold);
		this.measures = new IMultiLabelClassificationPredictionPerformanceMeasure[] { new ExactMatch(threshold), new F1MacroAverageL(threshold), new Hamming(threshold), new RankLoss(threshold) };
	}

	@Override
	public double loss(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> actual) {
		OptionalDouble res = Arrays.stream(this.measures).mapToDouble(x -> x.loss(expected, actual)).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("Could not take the average of all base measures");
		}
	}

}