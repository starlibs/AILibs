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

import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public class TimeTrackingLearnerWrapper extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IPrediction, IPredictionBatch> implements ITimeTrackingLearner {

	private List<Long> fitTimes;
	private List<Long> batchPredictTimes;
	private List<Long> perInstancePredictionTimes;

	private final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> wrappedSLClassifier;

	public TimeTrackingLearnerWrapper(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> wrappedLearner) {
		this.wrappedSLClassifier = wrappedLearner;
		this.fitTimes = new ArrayList<>();
		this.batchPredictTimes = new ArrayList<>();
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
	public List<Long> getBatchPredictionTimes() {
		return this.batchPredictTimes;
	}

	@Override
	public List<Long> getInstancePredictionTimes() {
		return this.perInstancePredictionTimes;
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

}
