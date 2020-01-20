package ai.libs.jaicore.search.experiments.terminationcriteria;

import ai.libs.jaicore.experiments.MaxNumberOfEventsTerminationCriterion;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class MaxRolloutExperimentTerminationCriterion extends MaxNumberOfEventsTerminationCriterion {

	public MaxRolloutExperimentTerminationCriterion(final int maxNumberOfRollouts) {
		super(maxNumberOfRollouts, RolloutEvent.class);
	}
}
