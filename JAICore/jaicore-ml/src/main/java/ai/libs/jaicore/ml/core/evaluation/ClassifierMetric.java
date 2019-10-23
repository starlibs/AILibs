package ai.libs.jaicore.ml.core.evaluation;

import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.execution.ILearnerRunReport;
import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.evaluation.loss.IMeasure;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;
import ai.libs.jaicore.ml.classification.singlelabel.loss.ErrorRate;

public enum ClassifierMetric implements ISupervisedLearnerMetric {

	MEAN_ERRORRATE(new ErrorRate(), new Mean());

	private final IMeasure lossFunction;
	private final IAggregateFunction<Double> aggregation;

	private ClassifierMetric(final IMeasure lossFunction, final IAggregateFunction<Double> aggregation) {
		this.lossFunction = lossFunction;
		this.aggregation = aggregation;
	}

	@Override
	public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
		return this.aggregation.aggregate(reports.stream().map(r -> (Double) this.lossFunction.loss(r.getPredictionDiffList())).collect(Collectors.toList()));
	}

	@Override
	public IMeasure getMeasure() {
		return this.lossFunction;
	}
}
