package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
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
	private final IDeterministicPredictionPerformanceMeasure<Object, Object> metric;

	public PreTrainedPredictionBasedClassifierEvaluator(final ILabeledDataset<?> testData, final IDeterministicPredictionPerformanceMeasure<Object, Object> metric) {
		super();
		this.testData = testData;
		this.metric = metric;
	}

	@Override
	public Double evaluate(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			return this.metric.loss(this.executor.execute(learner, this.testData).getPredictionDiffList());
		} catch (LearnerExecutionFailedException e) {
			throw new ObjectEvaluationFailedException(e);
		}
	}

}
