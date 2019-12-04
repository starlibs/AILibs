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
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainPredictionBasedClassifierEvaluator implements IClassifierEvaluator, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TrainPredictionBasedClassifierEvaluator.class);
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
			this.logger.info("Splitting the given data into two folds.");
			IDatasetSplitSet<ILabeledDataset<? extends ILabeledInstance>> splitSet = this.splitGenerator.nextSplitSet();
			if (splitSet.getNumberOfFoldsPerSplit() != 2) {
				throw new IllegalStateException("Number of folds for each split should be 2 but is " + splitSet.getNumberOfFoldsPerSplit() + "! Split generator: " + this.splitGenerator);
			}
			int n = splitSet.getNumberOfSplits();
			List<ILearnerRunReport> reports = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<ILabeledDataset<? extends ILabeledInstance>> folds = splitSet.getFolds(i);
				this.logger.debug("Executing learner on folds of sizes {} (train) and {} (test).", folds.get(0).size(), folds.get(1).size());
				reports.add(this.executor.execute(learner, folds.get(0), folds.get(1)));
			}
			return this.metric.evaluate(reports);
		} catch (LearnerExecutionFailedException | SplitFailedException e) {
			throw new ObjectEvaluationFailedException(e);
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.executor instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.executor).setLoggerName(name + ".executor");
			this.logger.info("Setting logger of learner executor {} to {}", this.executor.getClass().getName(), name + ".executor");
		}
		else {
			this.logger.info("Learner executor {} is not configurable for logging, so not configuring it.");
		}
	}
}
