package ai.libs.jaicore.ml.core.evaluation;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.jaicore.ml.classification.singlelabel.loss.ASingleLabelClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;

public class MLEvaluationUtil {

	public static double evaluate(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<? extends ILabeledInstance> data, final ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluator) throws ObjectEvaluationFailedException, InterruptedException {
		return evaluator.evaluate(learner);
	}

	public static double getLossForTrainedClassifier(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<? extends ILabeledInstance> testData, final ASingleLabelClassificationPerformanceMeasure measure) throws ObjectEvaluationFailedException, InterruptedException, LearnerExecutionFailedException {
		SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
		ILearnerRunReport report = executor.execute(learner, testData);
		return measure.loss(report.getPredictionDiffList());
	}

	public static double mccv(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainFoldSize, final long seed, final ClassifierMetric metric) throws ObjectEvaluationFailedException, InterruptedException {
		return evaluate(learner, data, new MonteCarloCrossValidationEvaluator(data, repeats, trainFoldSize, new Random(seed), metric));
	}

	public static double mccv(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainFoldSize, final long seed) throws ObjectEvaluationFailedException, InterruptedException {
		return mccv(learner, data, repeats, trainFoldSize, seed, ClassifierMetric.MEAN_ERRORRATE);
	}
}
