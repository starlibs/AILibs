package ai.libs.jaicore.ml.learningcurve.extrapolation;

import ai.libs.jaicore.basic.events.IEvent;

public class LearningCurveExtrapolatedEvent implements IEvent {
	private final LearningCurveExtrapolator extrapolator;

	public LearningCurveExtrapolatedEvent(final LearningCurveExtrapolator extrapolator) {
		super();
		this.extrapolator = extrapolator;
	}

	public LearningCurveExtrapolator getExtrapolator() {
		return this.extrapolator;
	}
}
