package ai.libs.jaicore.ml.core.evaluation.measure.singlelabel;

import ai.libs.jaicore.ml.core.evaluation.measure.IMeasure;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasure;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.EMultilabelPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.ExactMatchAccuracy;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.ExactMatchLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.F1MacroAverageL;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.F1MacroAverageLLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.HammingAccuracy;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.HammingLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1AsLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.JaccardLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.JaccardScore;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.RankLoss;
import ai.libs.jaicore.ml.core.evaluation.measure.multilabel.RankScore;

public class MultiClassMeasureBuilder {
	public IMeasure<Double, Double> getEvaluator(final EMultiClassPerformanceMeasure pm) {
		switch (pm) {
		case ERRORRATE:
			return new ZeroOneLoss();
		case MEAN_SQUARED_ERROR:
			return new MeanSquaredErrorLoss();
		case ROOT_MEAN_SQUARED_ERROR:
			return new RootMeanSquaredErrorLoss();
		case PRECISION:
			return new PrecisionAsLoss(0);
		default:
			throw new IllegalArgumentException("No support for performance measure " + pm);
		}
	}

	public IMeasure<double[], Double> getEvaluator(final EMultilabelPerformanceMeasure pm) {
		switch (pm) {
		case AUTO_MEKA_GGP_FITNESS:
			return new AutoMEKAGGPFitnessMeasure();
		case AUTO_MEKA_GGP_FITNESS_LOSS:
			return new AutoMEKAGGPFitnessMeasureLoss();
		case EXACT_MATCH_ACCURARY:
			return new ExactMatchAccuracy();
		case EXACT_MATCH_LOSS:
			return new ExactMatchLoss();
		case F1_MACRO_AVG_D:
			return new InstanceWiseF1();
		case F1_MACRO_AVG_D_LOSS:
			return new InstanceWiseF1AsLoss();
		case F1_MACRO_AVG_L:
			return new F1MacroAverageL();
		case F1_MACRO_AVG_L_LOSS:
			return new F1MacroAverageLLoss();
		case HAMMING_ACCURACY:
			return new HammingAccuracy();
		case HAMMING_LOSS:
			return new HammingLoss();
		case JACCARD_LOSS:
			return new JaccardLoss();
		case JACCARD_SCORE:
			return new JaccardScore();
		case RANK_LOSS:
			return new RankLoss();
		case RANK_SCORE:
			return new RankScore();
		default:
			throw new IllegalArgumentException("No support for performance measure " + pm);
		}
	}
}
