package hasco.core;

import java.util.Map;

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
	private final long timeOfCreation = System.currentTimeMillis();

	public HASCOSolutionCandidate(ComponentInstance componentInstance, EvaluatedSearchGraphBasedPlan<CEOCAction, V, ?> planningSolution) {
		super();
		this.componentInstance = componentInstance;
		this.planningSolution = planningSolution;
		if (planningSolution == null)
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a NULL planning solution.");
		if (planningSolution.getPath() == null)
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a planning solution that has a NULL path object.");
		if (planningSolution.getPath().getAnnotations() == null)
			throw new IllegalArgumentException("HASCOSolutionCandidate cannot be created with a planning solution that has a path object with a NULL annotations map.");
		if (!planningSolution.getPath().getAnnotations().containsKey("fTime"))
			throw new IllegalArgumentException("HASCO requires by definition that solutions are annotated with \"fTime\", which should tell the time that was necessary to evaluate the solution.");
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

	public Map<String, Object> getAnnotations() {
		return planningSolution.getPath().getAnnotations();
	}
	
	public Object getAnnotation(String key) {
		return planningSolution.getPath().getAnnotations().get(key);
	}
	
	public int getTimeToComputeScore() {
		return Integer.valueOf(getAnnotations().get("fTime").toString());
	}

	public long getTimeOfCreation() {
		return timeOfCreation;
	}
}
