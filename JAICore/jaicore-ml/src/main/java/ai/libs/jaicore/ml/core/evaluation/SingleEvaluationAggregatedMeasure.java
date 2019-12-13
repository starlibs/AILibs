package ai.libs.jaicore.ml.core.evaluation;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public class SingleEvaluationAggregatedMeasure<E, A> implements IAggregatedPredictionPerformanceMeasure<E, A> {

	private final IDeterministicPredictionPerformanceMeasure<E, A> lossFunction;

	public SingleEvaluationAggregatedMeasure(final IDeterministicPredictionPerformanceMeasure<E, A> lossFunction) {
		super();
		this.lossFunction = lossFunction;
	}

	@Override
	public double loss(final List<List<? extends E>> expected, final List<List<? extends A>> actual) {
		if (expected.size() != 1) {
			throw new IllegalArgumentException();
		}
		return this.lossFunction.loss(expected.get(0), actual.get(0));
	}

	@Override
	public double loss(final List<IPredictionAndGroundTruthTable<? extends E, ? extends A>> pairTables) {
		if (pairTables.size() != 1) {
			throw new IllegalArgumentException();
		}
		return this.lossFunction.loss(pairTables.get(0));
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
		return this.lossFunction;
	}
}
