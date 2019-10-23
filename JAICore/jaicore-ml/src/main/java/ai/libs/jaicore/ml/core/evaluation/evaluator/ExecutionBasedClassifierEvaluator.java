package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.classification.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.ai.ml.classification.execution.ILearnerRunReport;
import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.classification.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public class ExecutionBasedClassifierEvaluator<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements ISupervisedLearnerEvaluator<I, D> {

	private final IFixedDatasetSplitSetGenerator<D> splitGenerator;
	private final SupervisedLearnerExecutor<D> executor = new SupervisedLearnerExecutor<>();
	private final ISupervisedLearnerMetric metric;

	@Override
	public Double evaluate(final ISupervisedLearner<I, D> learner) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			IDatasetSplitSet<D> splitSet = this.splitGenerator.nextSplitSet();
			if (splitSet.getNumberOfFoldsPerSplit() != 2) {
				throw new IllegalStateException("Number of folds for each split should be 2 but is " + splitSet.getNumberOfFoldsPerSplit() + "!");
			}
			int n = splitSet.getNumberOfSplits();
			List<ILearnerRunReport> reports = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<D> folds = splitSet.getFolds(i);
				reports.add(this.executor.execute(learner, folds.get(0), folds.get(1)));
			}
			return this.metric.evaluate(reports);
		} catch (LearnerExecutionFailedException | SplitFailedException e) {
			throw new ObjectEvaluationFailedException(e);
		}
	}

	public ExecutionBasedClassifierEvaluator(final IFixedDatasetSplitSetGenerator<D> splitGenerator, final ISupervisedLearnerMetric metric) {
		super();
		this.splitGenerator = splitGenerator;
		this.metric = metric;
	}
}
