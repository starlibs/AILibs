package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;

public enum EAggregatedClassifierMetric implements IAggregatedPredictionPerformanceMeasure<Integer, ISingleLabelClassification> {

	MEAN_ERRORRATE(EClassificationPerformanceMeasure.ERRORRATE, new Mean());

	private final IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> lossFunction;
	private final IAggregateFunction<Double> aggregation;

	private EAggregatedClassifierMetric(final IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> lossFunction, final IAggregateFunction<Double> aggregation) {
		this.lossFunction = lossFunction;
		this.aggregation = aggregation;
	}

	@Override
	public double loss(final List<List<? extends Integer>> expected, final List<List<? extends ISingleLabelClassification>> predicted) {
		int n = expected.size();
		List<Double> losses = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			losses.add(this.lossFunction.loss(expected.get(i), predicted.get(i)));
		}
		return this.aggregation.aggregate(losses);
	}

	@Override
	public double loss(final List<IPredictionAndGroundTruthTable<? extends Integer, ? extends ISingleLabelClassification>> pairTables) {
		return this.aggregation.aggregate(pairTables.stream().map(this.lossFunction::loss).collect(Collectors.toList()));
	}

	@Override
	public double score(final List<List<? extends Integer>> expected, final List<List<? extends ISingleLabelClassification>> predicted) {
		return 1 - this.loss(expected, predicted);
	}

	@Override
	public double score(final List<IPredictionAndGroundTruthTable<? extends Integer, ? extends ISingleLabelClassification>> pairTables) {
		return 1 - this.loss(pairTables);
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> getBaseMeasure() {
		return this.lossFunction;
	}
}
