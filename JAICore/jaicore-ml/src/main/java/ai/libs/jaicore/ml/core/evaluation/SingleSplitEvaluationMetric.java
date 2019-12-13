package ai.libs.jaicore.ml.core.evaluation;

import java.util.Collection;

import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMetric;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public class SingleSplitEvaluationMetric implements IAggregatedPredictionPerformanceMetric {

	private final IDeterministicPredictionPerformanceMeasure lossFunction;

	public SingleSplitEvaluationMetric(final IDeterministicPredictionPerformanceMeasure lossFunction) {
		super();
		this.lossFunction = lossFunction;
	}

	@Override
	public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
		if (reports.size() != 1) {
			throw new IllegalArgumentException();
		}
		return this.lossFunction.loss(reports.iterator().next().getPredictionDiffList());
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure getMeasure() {
		return this.lossFunction;
	}
}
