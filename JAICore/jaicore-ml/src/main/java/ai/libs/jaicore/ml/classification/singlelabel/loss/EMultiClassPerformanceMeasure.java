package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public enum EMultiClassPerformanceMeasure implements IDeterministicPredictionPerformanceMeasure<Object> {

	//	AREA_ABOVE_ROC, AREA_UNDER_ROC, AVG_COST, CORRECT, CORRELATION_COEFFICIENT, ERROR_RATE, FALSE_NEGATIVE_RATE, FALSE_POSITIVE_RATE, F_MEASURE, INCORRECT, KAPPA, KB_INFORMATION, KB_MEA_INFORMATION, KB_RELATIVE_INFORMATION, MEAN_ABSOLUTE_ERROR, PCT_CORRECT, PCT_INCORRECT, PRECISION, RELATIVE_ABSOLUTE_ERROR, ROOT_MEAN_SQUARED_ERROR, ROOT_RELATIVE_SQUARED_ERROR, WEIGHTED_AREA_UNDER_ROC, WEIGHTED_FALSE_NEGATIVE_RATE, WEIGHTED_FALSE_POSITIVE_RATE, WEIGHTED_F_MEASURE, WEIGHTED_PRECISION, WEIGHTED_RECALL, WEIGHTED_TRUE_NEGATIVE_RATE, WEIGHTED_TRUE_POSITIVE_RATE
	ERRORRATE(new ErrorRate()), PRECISION_FIRST_CLASS_POSITIVE(new Precision(0)), PRECISION_SECOND_CLASS_POSITIVE(new Precision(1));

	private final IDeterministicPredictionPerformanceMeasure<Object> measure;

	private EMultiClassPerformanceMeasure(final IDeterministicPredictionPerformanceMeasure<Object> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<Object> actual, final List<Object> expected) {
		return this.measure.loss(actual, expected);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<Object> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final List<Object> expected, final List<Object> actual) {
		return this.measure.score(expected, actual);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<Object> pairTable) {
		return this.measure.score(pairTable);
	}
}
