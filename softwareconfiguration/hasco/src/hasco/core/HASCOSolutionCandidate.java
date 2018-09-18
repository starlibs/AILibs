package hasco.core;

import hasco.model.ComponentInstance;
import jaicore.planning.EvaluatedPlan;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.model.ceoc.CEOCAction;

/**
 * This is a wrapper class only used for efficient processing of solutions. For example, to lookup the annotations of a solution, we do not need the possibly costly equals method of T but only this
 * class. For each solution, only one such object is created.
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class HASCOSolutionCandidate<V extends Comparable<V>> {

	private final ComponentInstance componentInstance;
	private final EvaluatedSearchGraphBasedPlan<CEOCAction, V, ?> planningSolution;
	private final int timeToEvaluateCandidate;
	private final long timeOfCreation = System.currentTimeMillis();

	public HASCOSolutionCandidate(ComponentInstance componentInstance, EvaluatedSearchGraphBasedPlan<CEOCAction, V, ?> planningSolution, int timeToEvaluateCandidate) {
		super();
		this.componentInstance = componentInstance;
		this.planningSolution = planningSolution;
		this.timeToEvaluateCandidate = timeToEvaluateCandidate;
		if (planningSolution == null)
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a NULL planning solution.");
		if (planningSolution.getPath() == null)
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a planning solution that has a NULL path object.");
	}

	public ComponentInstance getComponentInstance() {
		return componentInstance;
	}

	public EvaluatedPlan<CEOCAction, V> getPlanningSolution() {
		return planningSolution;
	}
	
	public V getScore() {
		return planningSolution.getScore();
	}

	public int getTimeToEvaluateCandidate() {
		return timeToEvaluateCandidate;
	}

	public long getTimeOfCreation() {
		return timeOfCreation;
	}
}
