package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.evaluation.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public class TrainPredictionBasedClassifierEvaluator implements IClassifierEvaluator {

	private final IFixedDatasetSplitSetGenerator<ILabeledDataset<? extends ILabeledInstance>> splitGenerator;
	private final SupervisedLearnerExecutor<ILabeledDataset<? extends ILabeledInstance>> executor = new SupervisedLearnerExecutor<>();
	private final ISupervisedLearnerMetric metric;

	public TrainPredictionBasedClassifierEvaluator(final IFixedDatasetSplitSetGenerator<ILabeledDataset<?>> splitGenerator, final ISupervisedLearnerMetric metric) {
		super();
		this.splitGenerator = splitGenerator;
		this.metric = metric;
	}

	@Override
	public Double evaluate(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			IDatasetSplitSet<ILabeledDataset<? extends ILabeledInstance>> splitSet = this.splitGenerator.nextSplitSet();
			if (splitSet.getNumberOfFoldsPerSplit() != 2) {
				throw new IllegalStateException("Number of folds for each split should be 2 but is " + splitSet.getNumberOfFoldsPerSplit() + "! Split generator: " + this.splitGenerator);
			}
			int n = splitSet.getNumberOfSplits();
			List<ILearnerRunReport> reports = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<ILabeledDataset<? extends ILabeledInstance>> folds = splitSet.getFolds(i);
				reports.add(this.executor.execute(learner, folds.get(0), folds.get(1)));
			}
			return this.metric.evaluate(reports);
		} catch (LearnerExecutionFailedException | SplitFailedException e) {
			throw new ObjectEvaluationFailedException(e);
		}
	}
}
