package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

public enum EClassificationPerformanceMeasure implements IDeterministicHomogeneousPredictionPerformanceMeasure<Object> {

	// AREA_ABOVE_ROC, AREA_UNDER_ROC, AVG_COST, CORRECT, CORRELATION_COEFFICIENT, ERROR_RATE, FALSE_NEGATIVE_RATE, FALSE_POSITIVE_RATE, F_MEASURE, INCORRECT, KAPPA, KB_INFORMATION, KB_MEA_INFORMATION, KB_RELATIVE_INFORMATION,
	// MEAN_ABSOLUTE_ERROR, PCT_CORRECT, PCT_INCORRECT, PRECISION, RELATIVE_ABSOLUTE_ERROR, ROOT_MEAN_SQUARED_ERROR, ROOT_RELATIVE_SQUARED_ERROR, WEIGHTED_AREA_UNDER_ROC, WEIGHTED_FALSE_NEGATIVE_RATE, WEIGHTED_FALSE_POSITIVE_RATE,
	// WEIGHTED_F_MEASURE, WEIGHTED_PRECISION, WEIGHTED_RECALL, WEIGHTED_TRUE_NEGATIVE_RATE, WEIGHTED_TRUE_POSITIVE_RATE
	ERRORRATE(new ErrorRate()),
	TRUE_NEGATIVES_WITH_1_POSITIVE(new TrueNegatives(1)),
	TRUE_POSITIVES_WITH_1_POSITIVE(new TruePositives(1)),
	FALSE_NEGATIVES_WITH_1_POSITIVE(new FalseNegatives(1)),
	FALSE_POSITIVES_WITH_1_POSITIVE(new FalsePositives(1)),
	PRECISION_WITH_1_POSITIVE(new Precision(1)),
	RECALL_WITH_1_POSITIVE(new Recall(1)),
	F1_WITH_1_POSITIVE(new F1Measure(1));

	private final IDeterministicHomogeneousPredictionPerformanceMeasure<Object> measure;

	private EClassificationPerformanceMeasure(final IDeterministicHomogeneousPredictionPerformanceMeasure<Object> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<? extends Object> actual, final List<? extends Object> expected) {
		return this.measure.loss(actual, expected);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends Object, ? extends Object> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final List<? extends Object> expected, final List<? extends Object> actual) {
		return this.measure.score(expected, actual);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends Object, ? extends Object> pairTable) {
		return this.measure.score(pairTable);
	}
}
