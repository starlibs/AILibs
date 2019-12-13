package ai.libs.jaicore.ml.classification.multilabel.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
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

public enum EMultiLabelClassifierMetric implements IAggregatedPredictionPerformanceMeasure<Object, Object> {

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

		public double loss(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
			int n = expected.size();
			List<Double> losses = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				List<S> groundTruth = new ListView<S>(expected.get(i));
				List<T> predictions = new ListView<T>(actual.get(i));
				losses.add(this.lossFunction.loss(groundTruth, predictions));
			}
			return this.aggregation.aggregate(losses);
		}

		public double loss(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
			List<List<?>> expected = pairTables.stream().map(i -> i.getGroundTruthAsList()).collect(Collectors.toList());
			List<List<?>> actual = pairTables.stream().map(i -> i.getPredictionsAsList()).collect(Collectors.toList());
			return this.loss(expected, actual);
		}

		public double score(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
			return 1 - this.loss(expected, actual);
		}

		public double score(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
			return 1 - this.loss(pairTables);
		}
	}

	private final Wrapper<?, ?> wrapper;

	private <S, T> EMultiLabelClassifierMetric(final IMultiLabelClassificationPredictionPerformanceMeasure<S, T> lossFunction, final IAggregateFunction<Double> aggregation) {
		this.wrapper = new Wrapper<>(lossFunction, aggregation);
	}

	@Override
	public double loss(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
		return this.wrapper.loss(expected, actual);
	}

	@Override
	public double loss(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
		return this.wrapper.loss(pairTables);
	}

	@Override
	public double score(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
		return this.wrapper.score(expected, actual);
	}

	@Override
	public double score(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
		return this.wrapper.score(pairTables);
	}

}
