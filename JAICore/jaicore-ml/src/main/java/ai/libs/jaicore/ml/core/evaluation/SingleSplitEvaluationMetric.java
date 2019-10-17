package ai.libs.jaicore.ml.core.evaluation;

import java.util.Collection;

import org.api4.java.ai.ml.classification.execution.IClassifierMetric;
import org.api4.java.ai.ml.classification.execution.IClassifierRunReport;
import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

public class SingleSplitEvaluationMetric implements IClassifierMetric {

	private final ILossFunction lossFunction;

	public SingleSplitEvaluationMetric(final ILossFunction lossFunction) {
		super();
		this.lossFunction = lossFunction;
	}

	@Override
	public double evaluateToDouble(final Collection<? extends IClassifierRunReport> reports) {
		if (reports.size() != 1) {
			throw new IllegalArgumentException();
		}
		return this.lossFunction.loss(reports.iterator().next().getPredictionDiffList());
	}
}
