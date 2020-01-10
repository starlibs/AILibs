package ai.libs.jaicore.ml.core.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.aggregate.IRealsAggregateFunction;

public class AggregatingPredictionPerformanceMeasure<E, A> implements IAggregatedPredictionPerformanceMeasure<E, A> {

	private final IRealsAggregateFunction aggregator;
	private final IDeterministicPredictionPerformanceMeasure<E, A> baseMeasure;

	public AggregatingPredictionPerformanceMeasure(final IRealsAggregateFunction aggregator, final IDeterministicPredictionPerformanceMeasure<E, A> baseMeasure) {
		super();
		this.aggregator = aggregator;
		this.baseMeasure = baseMeasure;
	}

	@Override
	public double loss(final List<List<? extends E>> expected, final List<List<? extends A>> actual) {
		int n = expected.size();
		List<Double> losses = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			losses.add(this.baseMeasure.loss(expected.get(i), actual.get(i)));
		}
		return this.aggregator.aggregate(losses);
	}

	@Override
	public double loss(final List<IPredictionAndGroundTruthTable<? extends E, ? extends A>> pairTables) {
		List<List<? extends E>> expected = pairTables.stream().map(IPredictionAndGroundTruthTable::getGroundTruthAsList).collect(Collectors.toList());
		List<List<? extends A>> actual = pairTables.stream().map(IPredictionAndGroundTruthTable::getPredictionsAsList).collect(Collectors.toList());
		return this.loss(expected, actual);
	}

	@Override
	public double score(final List<List<? extends E>> expected, final List<List<? extends A>> actual) {
		return 1 - this.loss(expected, actual);
	}

	@Override
	public double score(final List<IPredictionAndGroundTruthTable<? extends E, ? extends A>> pairTables) {
		return 1 - this.loss(pairTables);
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<E, A> getBaseMeasure() {
		return this.baseMeasure;
	}
}
