package ai.libs.jaicore.planning.classical.problems.strips;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;

public class Operation implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1381223700100462982L;
	private final String name;
	private final Monom precondition;
	private final List<VariableParam> params;

	public Operation(final String name, final List<VariableParam> params, final Monom precondition) {
		super();
		this.name = name;
		this.params = params;
		this.precondition = precondition;
		for (Literal l : precondition) {
			Collection<VariableParam> missingParams = SetUtil.difference(l.getVariableParams(), params);
			if (!missingParams.isEmpty()) {
				throw new IllegalArgumentException("Operation " + name + " has parameters in the precondition that are not defined in the param list: " + missingParams);
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public Monom getPrecondition() {
		return this.precondition;
	}

	public List<VariableParam> getParams() {
		return this.params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.params == null) ? 0 : this.params.hashCode());
		result = prime * result + ((this.precondition == null) ? 0 : this.precondition.hashCode());
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
		Operation other = (Operation) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.params == null) {
			if (other.params != null) {
				return false;
			}
		} else if (!this.params.equals(other.params)) {
			return false;
		}
		if (this.precondition == null) {
			if (other.precondition != null) {
				return false;
			}
		} else if (!this.precondition.equals(other.precondition)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Operation [name=" + this.name + ", precondition=" + this.precondition + ", params=" + this.params + "]";
	}
}
