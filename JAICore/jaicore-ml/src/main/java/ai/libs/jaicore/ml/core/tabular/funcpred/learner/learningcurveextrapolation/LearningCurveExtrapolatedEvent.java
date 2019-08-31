package ai.libs.jaicore.ml.core.tabular.funcpred.learner.learningcurveextrapolation;

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
