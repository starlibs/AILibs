package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.mlplan.core.ITimeTrackingLearner;

public class TimeTrackingLearnerEvaluationEvent implements IEvent {

	private final long timestamp;
	private final IComponentInstance ci;
	private final ISupervisedLearner<?, ?> learner;
	private final Double actualFitTime;
	private final Double actualPredictTime;
	private final Double predictedFitTime;
	private final Double predictedPredictTime;
	private final Double score;

	@Override
	public String toString() {
		return "CI " + this.ci + " " + this.actualFitTime + " " + this.actualPredictTime + " " + this.predictedFitTime + " " + this.predictedPredictTime + " " + this.score;
	}

	public TimeTrackingLearnerEvaluationEvent(final ITimeTrackingLearner timeTrackingLearner) {
		this.timestamp = System.currentTimeMillis();
		this.ci = timeTrackingLearner.getComponentInstance();
		this.learner = timeTrackingLearner;
		this.actualFitTime = (!timeTrackingLearner.getFitTimes().isEmpty()) ? timeTrackingLearner.getFitTimes().stream().mapToDouble(x -> x).average().getAsDouble() / 1000 : null;
		this.actualPredictTime = (!timeTrackingLearner.getBatchPredictionTimesInMS().isEmpty()) ? timeTrackingLearner.getBatchPredictionTimesInMS().stream().mapToDouble(x -> x).average().getAsDouble() / 1000 : null;
		this.predictedFitTime = timeTrackingLearner.getPredictedInductionTime();
		this.predictedPredictTime = timeTrackingLearner.getPredictedInferenceTime();
		this.score = timeTrackingLearner.getScore();
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	public IComponentInstance getComponentInstance() {
		return this.ci;
	}

	public ISupervisedLearner<?, ?> getLearner() {
		return this.learner;
	}

	public Double getActualFitTime() {
		return this.actualFitTime;
	}

	public Double getActualPredictTime() {
		return this.actualPredictTime;
	}

	public Double getPredictedFitTime() {
		return this.predictedFitTime;
	}

	public Double getPredictedPredictTime() {
		return this.predictedPredictTime;
	}

	public Double getScore() {
		return this.score;
	}
}
