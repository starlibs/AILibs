package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

/**
 * This evaluator can be used to compute the performance of a pre-trained classifier on a given validation dataset
 *
 * @author Felix Mohr
 *
 */
public class PreTrainedPredictionBasedClassifierEvaluator implements IClassifierEvaluator {

	private final ILabeledDataset<?> testData;
	private final SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
	private final IAggregatedPredictionPerformanceMeasure<Object, Object> metric;

	public PreTrainedPredictionBasedClassifierEvaluator(final ILabeledDataset<?> testData, final IAggregatedPredictionPerformanceMeasure<Object, Object> metric) {
		super();
		this.testData = testData;
		this.metric = metric;
	}

	@Override
	public Double evaluate(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			List<ILearnerRunReport> reports = new ArrayList<>(1);
			reports.add(this.executor.execute(learner, this.testData));
			return this.metric.loss(reports.stream().map(r -> r.getPredictionDiffList()).collect(Collectors.toList()));
		} catch (LearnerExecutionFailedException e) {
			throw new ObjectEvaluationFailedException(e);
		}
	}

}
