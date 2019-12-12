package ai.libs.jaicore.ml.classification.multilabel.evaluation;

import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationMeasure;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.ISupervisedLearnerMetric;
import org.api4.java.common.aggregate.IAggregateFunction;

import ai.libs.jaicore.basic.aggregate.reals.Mean;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.ExactMatch;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.F1MacroAverageL;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.Hamming;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.JaccardScore;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.RankLoss;

public enum EMultiLabelClassifierMetric implements ISupervisedLearnerMetric {

	MEAN_EXACTMATCH(new ExactMatch(), new Mean()), MEAN_F1MACROL(new F1MacroAverageL(), new Mean()), MEAN_HAMMING(new Hamming(), new Mean()), MEAN_INSTANCEF1(new InstanceWiseF1(), new Mean()), MEAN_JACCARD(new JaccardScore(), new Mean()),
	MEAN_RANK(new RankLoss(), new Mean()), MEAN_AUTOMEKA_FITNESS(new AutoMEKAGGPFitnessMeasureLoss(), new Mean());

	private final IMultiLabelClassificationMeasure lossFunction;
	private final IAggregateFunction<Double> aggregation;

	private EMultiLabelClassifierMetric(final IMultiLabelClassificationMeasure lossFunction, final IAggregateFunction<Double> aggregation) {
		this.lossFunction = lossFunction;
		this.aggregation = aggregation;
	}

	@Override
	public double evaluateToDouble(final Collection<? extends ILearnerRunReport> reports) {
		return this.aggregation.aggregate(reports.stream().map(r -> (Double) this.lossFunction.loss((IMultiLabelClassificationPredictionAndGroundTruthTable) r.getPredictionDiffList())).collect(Collectors.toList()));
	}

	@Override
	public IMultiLabelClassificationMeasure getMeasure() {
		return this.lossFunction;
	}
}
