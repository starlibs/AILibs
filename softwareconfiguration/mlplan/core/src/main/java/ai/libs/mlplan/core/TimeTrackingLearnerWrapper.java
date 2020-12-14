package ai.libs.mlplan.core;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public class TimeTrackingLearnerWrapper extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IPrediction, IPredictionBatch> implements ITimeTrackingLearner, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TimeTrackingLearnerWrapper.class);

	private final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> wrappedSLClassifier;
	private IComponentInstance ci;

	private List<Long> fitTimes;
	private List<Long> batchPredictTimes;
	private List<Long> perInstancePredictionTimes;

	private Double predictedInductionTime = null;
	private Double predictedInferenceTime = null;
	private Double score;

	public TimeTrackingLearnerWrapper(final IComponentInstance ci, final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> wrappedLearner) {
		this.ci = ci;
		this.wrappedSLClassifier = wrappedLearner;
		this.fitTimes = new ArrayList<>();
		this.batchPredictTimes = new ArrayList<>();
		this.perInstancePredictionTimes = new ArrayList<>();
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		TimeTracker tracker = new TimeTracker();
		this.wrappedSLClassifier.fit(dTrain);
		this.fitTimes.add(tracker.stop());
	}

	@Override
	public IPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		TimeTracker tracker = new TimeTracker();
		IPrediction prediction = this.wrappedSLClassifier.predict(xTest);
		this.perInstancePredictionTimes.add(tracker.stop());
		return prediction;
	}

	@Override
	public IPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		TimeTracker tracker = new TimeTracker();
		IPredictionBatch prediction = this.wrappedSLClassifier.predict(dTest);
		long time = tracker.stop();
		this.batchPredictTimes.add(time);
		this.perInstancePredictionTimes.add(Math.round((double) time / dTest.length));
		return prediction;
	}

	@Override
	public List<Long> getFitTimes() {
		return this.fitTimes;
	}

	@Override
	public List<Long> getBatchPredictionTimesInMS() {
		return this.batchPredictTimes;
	}

	@Override
	public List<Long> getInstancePredictionTimesInMS() {
		return this.perInstancePredictionTimes;
	}

	@Override
	public IComponentInstance getComponentInstance() {
		return this.ci;
	}

	class TimeTracker {

		private final long startTime;

		private TimeTracker() {
			this.startTime = System.currentTimeMillis();
		}

		public long stop() {
			return System.currentTimeMillis() - this.startTime;
		}

	}

	@Override
	public void setPredictedInductionTime(final String inductionTime) {
		try {
			this.predictedInductionTime = Double.parseDouble(inductionTime);
		} catch (Exception e) {
			this.logger.warn("Could not parse double from provided induction time {}.", inductionTime, e);
		}
	}

	@Override
	public void setPredictedInferenceTime(final String inferenceTime) {
		try {
			this.predictedInferenceTime = Double.parseDouble(inferenceTime);
		} catch (Exception e) {
			this.logger.warn("Could not parse double from provided inference time {}.", inferenceTime, e);
		}
	}

	@Override
	public Double getPredictedInductionTime() {
		return this.predictedInductionTime;
	}

	@Override
	public Double getPredictedInferenceTime() {
		return this.predictedInferenceTime;
	}

	@Override
	public void setScore(final Double score) {
		if (score == null) {
			return;
		}
		this.score = score;
	}

	@Override
	public Double getScore() {
		return this.score;
	}

	@Override
	public ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearner() {
		return this.wrappedSLClassifier;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " -> " + this.wrappedSLClassifier.toString();
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.wrappedSLClassifier instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.wrappedSLClassifier).setLoggerName(name + ".bl");
		}
		else {
			this.logger.info("Underlying learner {} is not {}, so not customizing its logger.", this.wrappedSLClassifier.getClass(), ILoggingCustomizable.class);
		}
	}

}
