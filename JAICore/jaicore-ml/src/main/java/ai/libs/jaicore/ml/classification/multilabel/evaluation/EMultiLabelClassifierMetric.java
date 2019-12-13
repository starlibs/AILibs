package ai.libs.jaicore.ml.classification.multilabel.evaluation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMetric;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;
import ai.libs.jaicore.basic.sets.ListView;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.ExactMatch;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.F1MacroAverageL;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.Hamming;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.JaccardScore;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.RankLoss;

public enum EMultiLabelClassifierMetric implements IAggregatedPredictionPerformanceMetric {

	MEAN_EXACTMATCH(new ExactMatch(), new Mean()), MEAN_F1MACROL(new F1MacroAverageL(), new Mean()), MEAN_HAMMING(new Hamming(), new Mean()), MEAN_INSTANCEF1(new InstanceWiseF1(), new Mean()), MEAN_JACCARD(new JaccardScore(), new Mean()),
	MEAN_RANK(new RankLoss(), new Mean()), MEAN_AUTOMEKA_FITNESS(new AutoMEKAGGPFitnessMeasureLoss(), new Mean());

	private class Wrapper<S, T> {
		private final IMultiLabelClassificationPredictionPerformanceMeasure<S, T> lossFunction;
		private final IAggregateFunction<Double> aggregation;

		public Wrapper(final IMultiLabelClassificationPredictionPerformanceMeasure<S, T> lossFunction, final IAggregateFunction<Double> aggregation) {
			super();
			this.lossFunction = lossFunction;
			this.aggregation = aggregation;
		}

		public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
			return this.aggregation.aggregate(reports.stream().map(r -> {
				List<S> predictions = new ListView<S>(r.getPredictionDiffList().getPredictionsAsList());
				List<T> groundTruth = new ListView<T>(r.getPredictionDiffList().getGroundTruthAsList());
				return (Double)this.lossFunction.loss(groundTruth, predictions);
			}).collect(Collectors.toList()));
		}

		public IMultiLabelClassificationPredictionPerformanceMeasure getMeasure() {
			return this.lossFunction;
		}
	}

	private final Wrapper<?, ?> wrapper;

	private <S, T> EMultiLabelClassifierMetric(final IMultiLabelClassificationPredictionPerformanceMeasure<S, T> lossFunction, final IAggregateFunction<Double> aggregation) {
		this.wrapper = new Wrapper<>(lossFunction, aggregation);
	}

	@Override
	public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
		return this.wrapper.evaluateToDouble(reports);
	}

	@Override
	public IMultiLabelClassificationPredictionPerformanceMeasure<?, ?> getMeasure() {
		return this.wrapper.getMeasure();
	}
}
