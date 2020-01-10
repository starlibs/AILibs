package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;

public enum EAggregatedClassifierMetric implements IAggregatedPredictionPerformanceMeasure<Object, Object> {

	MEAN_ERRORRATE(EClassificationPerformanceMeasure.ERRORRATE, new Mean());

	private final IDeterministicHomogeneousPredictionPerformanceMeasure<Object> lossFunction;
	private final IAggregateFunction<Double> aggregation;

	private EAggregatedClassifierMetric(final IDeterministicHomogeneousPredictionPerformanceMeasure<Object> lossFunction, final IAggregateFunction<Double> aggregation) {
		this.lossFunction = lossFunction;
		this.aggregation = aggregation;
	}

	@Override
	public double loss(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
		int n = expected.size();
		List<Double> losses = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			losses.add(this.lossFunction.loss(expected.get(i), actual.get(i)));
		}
		return this.aggregation.aggregate(losses);
	}

	@Override
	public double loss(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
		return this.aggregation.aggregate(pairTables.stream().map(this.lossFunction::loss).collect(Collectors.toList()));
	}

	@Override
	public double score(final List<List<? extends Object>> expected, final List<List<? extends Object>> actual) {
		return 1 - this.loss(expected, actual);
	}

	@Override
	public double score(final List<IPredictionAndGroundTruthTable<? extends Object, ? extends Object>> pairTables) {
		return 1 - this.loss(pairTables);
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<Object, Object> getBaseMeasure() {
		return this.lossFunction;
	}
}
