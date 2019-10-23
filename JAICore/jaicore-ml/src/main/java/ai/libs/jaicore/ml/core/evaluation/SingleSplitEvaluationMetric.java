package ai.libs.jaicore.ml.core.evaluation;

import java.util.Collection;

import org.api4.java.ai.ml.classification.execution.ILearnerRunReport;
import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.evaluation.loss.IMeasure;

public class SingleSplitEvaluationMetric implements ISupervisedLearnerMetric {

	private final IMeasure lossFunction;

	public SingleSplitEvaluationMetric(final IMeasure lossFunction) {
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
	public IMeasure getMeasure() {
		return this.lossFunction;
	}
}
