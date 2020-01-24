package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.evaluation.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionInterruptedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationCompletedEvent;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationFailedEvent;

public class TrainPredictionBasedClassifierEvaluator implements IClassifierEvaluator, ILoggingCustomizable, IEventEmitter<Object> {

	private Logger logger = LoggerFactory.getLogger(TrainPredictionBasedClassifierEvaluator.class);
	private final IFixedDatasetSplitSetGenerator<ILabeledDataset<? extends ILabeledInstance>> splitGenerator;
	private final SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
	private final IAggregatedPredictionPerformanceMeasure metric;
	private final EventBus eventBus = new EventBus();
	private boolean hasListeners;

	public TrainPredictionBasedClassifierEvaluator(final IFixedDatasetSplitSetGenerator<ILabeledDataset<?>> splitGenerator, final IAggregatedPredictionPerformanceMeasure<?, ?> metric) {
		super();
		this.splitGenerator = splitGenerator;
		this.metric = metric;
	}

	@Override
	public Double evaluate(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			long evaluationStart = System.currentTimeMillis();
			this.logger.info("Using {} to split the given data into two folds.", this.splitGenerator.getClass().getName());
			IDatasetSplitSet<ILabeledDataset<? extends ILabeledInstance>> splitSet = this.splitGenerator.nextSplitSet();
			if (splitSet.getNumberOfFoldsPerSplit() != 2) {
				throw new IllegalStateException("Number of folds for each split should be 2 but is " + splitSet.getNumberOfFoldsPerSplit() + "! Split generator: " + this.splitGenerator);
			}
			int n = splitSet.getNumberOfSplits();
			List<ILearnerRunReport> reports = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<ILabeledDataset<? extends ILabeledInstance>> folds = splitSet.getFolds(i);
				this.logger.debug("Executing learner{} on folds of sizes {} (train) and {} (test) using {}.", learner, folds.get(0).size(), folds.get(1).size(), this.executor.getClass().getName());
				ILearnerRunReport report;
				try {
					report = this.executor.execute(learner, folds.get(0), folds.get(1));
					this.logger.trace("Obtained report. Training times was {}ms, testing time {}ms. Ground truth vector: {}, prediction vector: {}", report.getTrainEndTime() - report.getTrainStartTime(),
							report.getTestEndTime() - report.getTestStartTime(), report.getPredictionDiffList().getGroundTruthAsList(), report.getPredictionDiffList().getPredictionsAsList());
				} catch (LearnerExecutionInterruptedException e) {
					this.logger.info("Received interrupt of training in iteration #{} after a total evaluation time of {}ms. Sending an event over the bus and forwarding the exception.", i + 1, System.currentTimeMillis() - evaluationStart);
					ILabeledDataset<?> train = folds.get(0);
					ILabeledDataset<?> test = folds.get(1);
					ILearnerRunReport failReport = new LearnerRunReport(train, test, e.getTrainTimeStart(), e.getTrainTimeEnd(), e.getTestTimeStart(), e.getTestTimeEnd(), e);
					this.eventBus.post(new TrainTestSplitEvaluationFailedEvent<>(learner, failReport));
					throw e;
				} catch (LearnerExecutionFailedException e) { // cannot be merged with the above clause, because then the only common supertype is "Exception", which does not have these methods
					this.logger.info("Catching {} in iteration #{} after a total evaluation time of {}ms. Sending an event over the bus and forwarding the exception.", e.getClass().getName(), i + 1,
							System.currentTimeMillis() - evaluationStart);
					ILabeledDataset<?> train = folds.get(0);
					ILabeledDataset<?> test = folds.get(1);
					ILearnerRunReport failReport = new LearnerRunReport(train, test, e.getTrainTimeStart(), e.getTrainTimeEnd(), e.getTestTimeStart(), e.getTestTimeEnd(), e);
					this.eventBus.post(new TrainTestSplitEvaluationFailedEvent<>(learner, failReport));
					throw e;
				}

				if (this.hasListeners) {
					this.eventBus.post(new TrainTestSplitEvaluationCompletedEvent<>(learner, report));
				}
				reports.add(report);
				if (this.logger.isDebugEnabled()) {
					List<?> gt = report.getPredictionDiffList().getGroundTruthAsList();
					List<?> pr = report.getPredictionDiffList().getPredictionsAsList();
					int m = gt.size();
					int mistakes = 0;
					for (int j = 0; j < m; j++) {
						if (!pr.get(j).equals(gt.get(j))) {
							mistakes++;
						}
					}
					if (m - mistakes == 0) {
						this.logger.warn("0 correct predictions seems suspicious. Here are the vectors: \n\tGround truth: {}\n\tPredictions: {}", report.getPredictionDiffList().getGroundTruthAsList(),
								report.getPredictionDiffList().getPredictionsAsList());
					}
					this.logger.debug("Execution completed. Classifier predicted {}/{} test instances correctly.", (m - mistakes), m);
				}
			}
			this.logger.debug("Compute metric ({}) for the diff of predictions and ground truth.", this.metric.getClass().getName());
			double score = this.metric.loss(reports.stream().map(ILearnerRunReport::getPredictionDiffList).collect(Collectors.toList()));
			this.logger.info("Computed value for metric {} of {} executions. Metric value is: {}", this.metric, n, score);
			return score;
		} catch (LearnerExecutionFailedException | SplitFailedException e) {
			this.logger.debug("Failed to evaluate the classifier {}.", e);
			throw new ObjectEvaluationFailedException(e);
		}
	}

	protected IFixedDatasetSplitSetGenerator<ILabeledDataset<? extends ILabeledInstance>> getSplitGenerator() {
		return this.splitGenerator;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.splitGenerator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.splitGenerator).setLoggerName(name + ".splitgen");
			this.logger.info("Setting logger of split generator {} to {}.splitgen", this.splitGenerator.getClass().getName(), name);
		} else {
			this.logger.info("Split generator {} is not configurable for logging, so not configuring it.", this.splitGenerator.getClass().getName());
		}
		this.executor.setLoggerName(name + ".executor");
		this.logger.info("Setting logger of learner executor {} to {}.executor", this.executor.getClass().getName(), name);
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
		this.hasListeners = true;
	}
}
