package jaicore.planning.model.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logic.fol.structure.Monom;

public class Plan<A extends Action> {
	private final List<A> actions;
	private final Map<String,Object> annotations;

	public Plan(List<A> actions) {
		this(actions, new HashMap<>());
	}
	
	public Plan(List<A> actions, Map<String, Object> annotations) {
		super();
		this.actions = actions;
		this.annotations = annotations;
	}

	public List<A> getActions() {
		return actions;
	}
	
	public void setAnnotation(String key, Object value) {
		annotations.put(key, value);
	}
	
	public void setAnnotation(Map<String, Object> annotations) {
		annotations.putAll(annotations);
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}
	
	public Monom getStateAfterApplicationGivenInitState(Monom initState) {
		Monom newState = new Monom(initState);
		for (A action : actions)
			PlannerUtil.updateState(newState, action);
		return newState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plan other = (Plan) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		return true;
	}

}
