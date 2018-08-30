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

	public HASCOSolutionCandidate(ComponentInstance componentInstance, EvaluatedSearchGraphBasedPlan<CEOCAction, V, ?> planningSolution) {
		super();
		this.componentInstance = componentInstance;
		this.planningSolution = planningSolution;
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
	
	public int getTimeToComputePathScore() {
		return Integer.valueOf(getAnnotations().get("fTime").toString());
	}
}
