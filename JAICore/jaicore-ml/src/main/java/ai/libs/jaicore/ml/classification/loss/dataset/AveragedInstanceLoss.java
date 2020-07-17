package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;

public class AveragedInstanceLoss extends ASingleLabelClassificationPerformanceMeasure {

	private IDeterministicInstancePredictionPerformanceMeasure<ISingleLabelClassification, Integer> instanceMeasure;

	public AveragedInstanceLoss(final IDeterministicInstancePredictionPerformanceMeasure<ISingleLabelClassification, Integer> instanceMeasure) {
		this.instanceMeasure = instanceMeasure;
	}

	@Override
	public double loss(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		return IntStream.range(0, expected.size()).mapToDouble(x -> this.instanceMeasure.loss(expected.get(x), predicted.get(x))).average().getAsDouble();
	}

}
