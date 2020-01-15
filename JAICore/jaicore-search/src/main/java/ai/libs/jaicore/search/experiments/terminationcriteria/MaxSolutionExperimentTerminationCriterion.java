package ai.libs.jaicore.search.experiments.terminationcriteria;

import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;

import ai.libs.jaicore.experiments.MaxNumberOfEventsTerminationCriterion;

public class MaxSolutionExperimentTerminationCriterion extends MaxNumberOfEventsTerminationCriterion {

	public MaxSolutionExperimentTerminationCriterion(final int maxNumberOfSolutions) {
		super(maxNumberOfSolutions, ISolutionCandidateFoundEvent.class);
	}
}
