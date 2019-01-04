package jaicore.planning.classical.problems.strips;

import java.io.Serializable;
import java.util.List;

import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class Operation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1381223700100462982L;
	private final String name;
	private final Monom precondition;
	private final List<VariableParam> params;

	public Operation(String name, List<VariableParam> params, Monom precondition) {
		super();
		this.name = name;
		this.params = params;
		this.precondition = precondition;
	}

	public String getName() {
		return name;
	}

	public Monom getPrecondition() {
		return precondition;
	}

	public List<VariableParam> getParams() {
		return params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((precondition == null) ? 0 : precondition.hashCode());
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
		Operation other = (Operation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (precondition == null) {
			if (other.precondition != null)
				return false;
		} else if (!precondition.equals(other.precondition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Operation [name=" + name + ", precondition=" + precondition + ", params=" + params + "]";
	}
}
