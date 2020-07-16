package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;

/**
 * Instance-wise F1 measure for multi-label classifiers.
 *
 * For reference see
 * Wu, Xi-Zhu; Zhou, Zhi-Hua: A Unified View of Multi-Label Performance Measures (ICML / JMLR 2017)
 *
 * @author mwever
 *
 */
public class InstanceWiseF1 extends AMultiLabelClassificationMeasure {

	public InstanceWiseF1(final double threshold) {
		super(threshold);
	}

	public InstanceWiseF1() {
		super();
	}

	@Override
	public double score(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);
		int[][] expectedMatrix = this.listToMatrix(expected);
		int[][] actualMatrix = this.listToThresholdedRelevanceMatrix(predicted);
		F1Measure baseMeasure = new F1Measure(1);
		OptionalDouble res = IntStream.range(0, expectedMatrix.length).mapToDouble(x -> baseMeasure.score(Arrays.stream(expectedMatrix[x]).mapToObj(Integer::valueOf).collect(Collectors.toList()),
				Arrays.stream(actualMatrix[x]).mapToObj(y -> new SingleLabelClassification(2, y)).collect(Collectors.toList()))).average();

		if (!res.isPresent()) {
			throw new IllegalStateException("Could not determine average instance-wise f measure.");
		} else {
			return res.getAsDouble();
		}
	}
}
