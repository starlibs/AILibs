package ai.libs.jaicore.planning.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.algorithms.strips.forward.StripsUtil;

public class Plan {
	private final List<Action> actions;
	private final Map<String,Object> annotations;

	public Plan(final List<Action> actions) {
		this(actions, new HashMap<>());
	}

	public Plan(final List<Action> actions, final Map<String, Object> annotations) {
		super();
		this.actions = actions;
		this.annotations = annotations;
	}

	public List<Action> getActions() {
		return this.actions;
	}

	public void setAnnotation(final String key, final Object value) {
		this.annotations.put(key, value);
	}

	public void setAnnotation(final Map<String, Object> annotations) {
		annotations.putAll(annotations);
	}

	public Map<String, Object> getAnnotations() {
		return this.annotations;
	}

	public Monom getStateAfterApplicationGivenInitState(final Monom initState) {
		Monom newState = new Monom(initState);
		for (Action action : this.actions) {
			StripsUtil.updateState(newState, action);
		}
		return newState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.actions == null) ? 0 : this.actions.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Plan other = (Plan) obj;
		if (this.actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!this.actions.equals(other.actions)) {
			return false;
		}
		return true;
	}

}
