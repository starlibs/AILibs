package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.ISupervisedLearnerExecutor;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionInterruptedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;

public class SupervisedLearnerExecutor implements ISupervisedLearnerExecutor, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SupervisedLearnerExecutor.class);

	@Override
	public <I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> ILearnerRunReport execute(final ISupervisedLearner<I, D> learner, final D train, final D test)
			throws LearnerExecutionFailedException, LearnerExecutionInterruptedException {
		long startTrainTime = System.currentTimeMillis();
		try {
			this.logger.info("Fitting the learner (class: {}) {} with {} instances, each of which with {} attributes", learner.getClass().getName(), learner, train.size(), train.getNumAttributes());
			learner.fit(train);
		} catch (InterruptedException e) {
			long now = System.currentTimeMillis();
			this.logger.info("Training was interrupted after {}ms, sending respective LearnerExecutionInterruptedException.", now - startTrainTime);
			throw new LearnerExecutionInterruptedException(startTrainTime, now);
		} catch (TrainingException e) {
			long now = System.currentTimeMillis();
			this.logger.info("Training failed due to an {} after {}ms.", e.getClass().getName(), now - startTrainTime);
			throw new LearnerExecutionFailedException(startTrainTime, now, e);
		}
		long endTrainTime = System.currentTimeMillis();
		this.logger.debug("Training finished successfully after {}ms. Now acquiring predictions.", endTrainTime - startTrainTime);
		try {
			ILearnerRunReport report = this.getReportForTrainedLearner(learner, train, test, startTrainTime, endTrainTime);
			long now = System.currentTimeMillis();
			this.logger.info("Run report ready after {}ms with {} comparisons of predictions and ground truth.", now - endTrainTime, report.getPredictionDiffList().size());
			return report;
		} catch (InterruptedException e) {
			long now = System.currentTimeMillis();
			this.logger.info("Learner was interrupted during prediction after a runtime of {}ms for training and {}ms for testing ({}ms total walltime).", endTrainTime - startTrainTime, now - endTrainTime, now - startTrainTime);
			if (Thread.currentThread().isInterrupted()) {
				this.logger.warn("Observed an InterruptedException while evaluating a learner of type {} ({}) AND the thread is interrupted. This should never happen! Here is the detailed information: {}", learner.getClass(), learner,
						LoggerUtil.getExceptionInfo(e));
			}
			throw new LearnerExecutionInterruptedException(startTrainTime, endTrainTime, endTrainTime, System.currentTimeMillis());
		} catch (PredictionException e) {
			this.logger.info("Prediction failed with exception {}.", e.getClass().getName());
			throw new LearnerExecutionFailedException(startTrainTime, endTrainTime, endTrainTime, System.currentTimeMillis(), e);
		}
	}

	@Override
	public <I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> ILearnerRunReport execute(final ISupervisedLearner<I, D> learner, final D test) throws LearnerExecutionFailedException {
		long startTestTime = System.currentTimeMillis();
		try {
			return this.getReportForTrainedLearner(learner, null, test, -1, -1);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LearnerExecutionFailedException(-1, -1, startTestTime, System.currentTimeMillis(), e);
		} catch (PredictionException e) {
			throw new LearnerExecutionFailedException(-1, -1, startTestTime, System.currentTimeMillis(), e);
		}
	}

	private <I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> ILearnerRunReport getReportForTrainedLearner(final ISupervisedLearner<I, D> learner, final D train, final D test, final long trainingStartTime,
			final long trainingEndTime) throws PredictionException, InterruptedException {
		String previousLoggerName = null;
		if (learner instanceof ILoggingCustomizable) {
			previousLoggerName = ((ILoggingCustomizable) learner).getLoggerName();
			String tmpLoggerName = this.getLoggerName() + ".learner";
			this.logger.debug("Temporarily switching logger of learner {} from {} to {}", learner.getClass(), previousLoggerName, tmpLoggerName);
			((ILoggingCustomizable) learner).setLoggerName(tmpLoggerName);
		}
		else {
			this.logger.debug("Evaluated learner {} is not {}, so not customizing its logger.", learner.getClass(), ILoggingCustomizable.class);
		}
		long start = System.currentTimeMillis();
		List<? extends IPrediction> predictions = learner.predict(test).getPredictions();
		long endTestTime = System.currentTimeMillis();

		/* create difference table */
		TypelessPredictionDiff diff = new TypelessPredictionDiff();
		for (int i = 0; i < predictions.size(); i++) {
			diff.addPair(test.get(i).getLabel(), predictions.get(i));
		}
		if (learner instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) learner).setLoggerName(previousLoggerName);
		}
		return new LearnerRunReport(train, test, trainingStartTime, trainingEndTime, start, endTestTime, diff);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
