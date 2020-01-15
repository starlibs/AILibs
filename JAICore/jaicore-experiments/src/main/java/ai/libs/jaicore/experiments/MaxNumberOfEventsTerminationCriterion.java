package ai.libs.jaicore.experiments;

import java.util.Arrays;
import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;

public class MaxNumberOfEventsTerminationCriterion implements IExperimentTerminationCriterion {
	private final int maxNumberOfEvents;
	private final List<Class<? extends IAlgorithmEvent>> matchedClasses;
	private int numOfSeenEvents = 0;

	public MaxNumberOfEventsTerminationCriterion(final int maxNumberOfEvents, final Class<? extends IAlgorithmEvent> matchedClass) {
		this(maxNumberOfEvents, Arrays.asList(matchedClass));
	}

	public MaxNumberOfEventsTerminationCriterion(final int maxNumberOfEvents, final List<Class<? extends IAlgorithmEvent>> matchedClasses) {
		super();
		this.maxNumberOfEvents = maxNumberOfEvents;
		this.matchedClasses = matchedClasses;
	}

	@Override
	public boolean doesTerminate(final IAlgorithmEvent e, final IAlgorithm<?, ?> algorithm) {
		if (this.matchedClasses.stream().anyMatch(c -> c.isInstance(e))) {
			this.numOfSeenEvents ++;
			if (this.numOfSeenEvents >= this.maxNumberOfEvents) {
				return true;
			}
		}
		return false;
	}
}
