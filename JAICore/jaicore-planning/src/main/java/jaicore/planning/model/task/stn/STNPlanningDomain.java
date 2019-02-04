package jaicore.planning.model.task.stn;

import java.io.Serializable;
import java.util.Collection;

import jaicore.planning.model.core.Operation;

@SuppressWarnings("serial")
public class STNPlanningDomain<O extends Operation, M extends Method> implements Serializable {

	private final Collection<O> operations;
	private final Collection<M> methods;

	public STNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super();
		this.operations = operations;
		this.methods = methods;
	}

	public Collection<O> getOperations() {
		return operations;
	}

	public Collection<M> getMethods() {
		return methods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		result = prime * result + ((operations == null) ? 0 : operations.hashCode());
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
		STNPlanningDomain other = (STNPlanningDomain) obj;
		if (methods == null) {
			if (other.methods != null)
				return false;
		} else if (!methods.equals(other.methods))
			return false;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "STNPlanningDomain [operations=" + operations + ", methods=" + methods + "]";
	}
}
