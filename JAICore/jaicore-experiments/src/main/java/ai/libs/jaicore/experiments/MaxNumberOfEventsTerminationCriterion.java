package ai.libs.jaicore.experiments;

import java.util.Arrays;
import java.util.List;

import org.api4.java.algorithm.events.AlgorithmEvent;

public class MaxNumberOfEventsTerminationCriterion implements IExperimentTerminationCriterion {
	private final int maxNumberOfEvents;
	private final List<Class<? extends AlgorithmEvent>> matchedClasses;
	private int numOfSeenEvents = 0;

	public MaxNumberOfEventsTerminationCriterion(final int maxNumberOfEvents, final Class<? extends AlgorithmEvent> matchedClass) {
		this(maxNumberOfEvents, Arrays.asList(matchedClass));
	}

	public MaxNumberOfEventsTerminationCriterion(final int maxNumberOfEvents, final List<Class<? extends AlgorithmEvent>> matchedClasses) {
		super();
		this.maxNumberOfEvents = maxNumberOfEvents;
		this.matchedClasses = matchedClasses;
	}

	@Override
	public boolean doesTerminate(final AlgorithmEvent e) {
		if (this.matchedClasses.stream().anyMatch(c -> c.isInstance(e))) {
			this.numOfSeenEvents ++;
			if (this.numOfSeenEvents >= this.maxNumberOfEvents) {
				return true;
			}
		}
		return false;
	}
}
