package jaicore.planning.model.task.stn;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.model.core.Operation;

@SuppressWarnings("serial")
public class STNPlanningDomain<O extends Operation, M extends Method> implements Serializable {

	private final Collection<O> operations;
	private final Collection<M> methods;

	public STNPlanningDomain(final Collection<O> operations, final Collection<M> methods) {
		super();
		this.operations = operations;
		this.methods = methods;
	}

	public Collection<O> getOperations() {
		return this.operations;
	}

	public Collection<M> getMethods() {
		return this.methods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.methods == null) ? 0 : this.methods.hashCode());
		result = prime * result + ((this.operations == null) ? 0 : this.operations.hashCode());
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
		STNPlanningDomain other = (STNPlanningDomain) obj;
		if (this.methods == null) {
			if (other.methods != null) {
				return false;
			}
		} else if (!this.methods.equals(other.methods)) {
			return false;
		}
		if (this.operations == null) {
			if (other.operations != null) {
				return false;
			}
		} else if (!this.operations.equals(other.operations)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("operations", this.operations);
		fields.put("methods", this.methods);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
