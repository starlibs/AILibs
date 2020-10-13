package ai.libs.hasco.core;

import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;

/**
 * This is a wrapper class only used for efficient processing of solutions. For example, to lookup the annotations of a solution, we do not need the possibly costly equals method of T but only this
 * class. For each solution, only one such object is created.
 *
 * @author fmohr
 *
 * @param <T>
 */
public class HASCOSolutionCandidate<V extends Comparable<V>> implements IEvaluatedSoftwareConfigurationSolution<V> {

	private final ComponentInstance componentInstance;
	private final IEvaluatedGraphSearchBasedPlan<?, ?, V> planningSolution;
	private final int timeToEvaluateCandidate;
	private final long timeOfCreation = System.currentTimeMillis();

	public HASCOSolutionCandidate(final ComponentInstance componentInstance, final IEvaluatedGraphSearchBasedPlan<?, ?, V> planningSolution, final int timeToEvaluateCandidate) {
		super();
		this.componentInstance = componentInstance;
		this.planningSolution = planningSolution;
		this.timeToEvaluateCandidate = timeToEvaluateCandidate;
		if (planningSolution == null) {
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a NULL planning solution.");
		}
		if (planningSolution.getSearchGraphPath() == null) {
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a planning solution that has a NULL path object.");
		}
	}

	@Override
	public ComponentInstance getComponentInstance() {
		return this.componentInstance;
	}

	@Override
	public V getScore() {
		return this.planningSolution.getScore();
	}

	public int getTimeToEvaluateCandidate() {
		return this.timeToEvaluateCandidate;
	}

	public long getTimeOfCreation() {
		return this.timeOfCreation;
	}
}
