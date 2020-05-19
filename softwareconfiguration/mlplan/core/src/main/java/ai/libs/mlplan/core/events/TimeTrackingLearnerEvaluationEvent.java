package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.mlplan.core.ITimeTrackingLearner;

public class TimeTrackingLearnerEvaluationEvent implements IEvent {

	private final long timestamp;
	private final ComponentInstance ci;
	private final ISupervisedLearner<?, ?> learner;
	private final Double actualFitTime;
	private final Double actualPredictTime;
	private final Double predictedFitTime;
	private final Double predictedPredictTime;

	public TimeTrackingLearnerEvaluationEvent(final ITimeTrackingLearner timeTrackingLearner) {
		this.timestamp = System.currentTimeMillis();
		this.ci = timeTrackingLearner.getComponentInstance();
		this.learner = timeTrackingLearner;
		this.actualFitTime = (!timeTrackingLearner.getFitTimes().isEmpty()) ? timeTrackingLearner.getFitTimes().stream().mapToDouble(x -> x).average().getAsDouble() : null;
		this.actualPredictTime = (!timeTrackingLearner.getBatchPredictionTimes().isEmpty()) ? timeTrackingLearner.getBatchPredictionTimes().stream().mapToDouble(x -> x).average().getAsDouble() : null;
		this.predictedFitTime = timeTrackingLearner.getPredictedInductionTime();
		this.predictedPredictTime = timeTrackingLearner.getPredictedInferenceTime();
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	public ComponentInstance getComponentInstance() {
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

}
