package ai.libs.jaicore.ml.core.evaluation;

import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMetric;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;
import ai.libs.jaicore.ml.classification.singlelabel.loss.ErrorRate;

public enum EAggregatedClassifierMetric implements IAggregatedPredictionPerformanceMetric {

	MEAN_ERRORRATE(new ErrorRate(), new Mean());

	private final IDeterministicPredictionPerformanceMeasure lossFunction;
	private final IAggregateFunction<Double> aggregation;

	private EAggregatedClassifierMetric(final IDeterministicPredictionPerformanceMeasure lossFunction, final IAggregateFunction<Double> aggregation) {
		this.lossFunction = lossFunction;
		this.aggregation = aggregation;
	}

	@Override
	public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
		return this.aggregation.aggregate(reports.stream().map(r -> (Double) this.lossFunction.loss(r.getPredictionDiffList())).collect(Collectors.toList()));
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure getMeasure() {
		return this.lossFunction;
	}
}
