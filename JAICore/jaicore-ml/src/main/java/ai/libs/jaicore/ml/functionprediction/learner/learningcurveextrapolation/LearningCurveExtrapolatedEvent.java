package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import org.api4.java.common.event.IEvent;

public class LearningCurveExtrapolatedEvent implements IEvent {
	private final LearningCurveExtrapolator extrapolator;

	public LearningCurveExtrapolatedEvent(final LearningCurveExtrapolator extrapolator) {
		super();
		this.extrapolator = extrapolator;
	}

	public LearningCurveExtrapolator getExtrapolator() {
		return this.extrapolator;
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException();
	}
}
