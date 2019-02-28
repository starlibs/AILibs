package jaicore.planning.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.algorithms.strips.forward.StripsUtil;

public class Plan {
	private final List<Action> actions;
	private final Map<String,Object> annotations;

	public Plan(List<Action> actions) {
		this(actions, new HashMap<>());
	}
	
	public Plan(List<Action> actions, Map<String, Object> annotations) {
		super();
		this.actions = actions;
		this.annotations = annotations;
	}

	public List<Action> getActions() {
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
		for (Action action : actions)
			StripsUtil.updateState(newState, action);
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
