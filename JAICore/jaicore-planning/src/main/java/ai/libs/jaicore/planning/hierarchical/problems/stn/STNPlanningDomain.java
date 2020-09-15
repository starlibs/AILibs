package ai.libs.jaicore.planning.hierarchical.problems.stn;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;

@SuppressWarnings("serial")
public class STNPlanningDomain implements Serializable {

	private final Collection<? extends Operation> operations;
	private final Collection<? extends Method> methods;

	public STNPlanningDomain(final Collection<? extends Operation> operations, final Collection<? extends Method> methods) {
		super();
		Set<String> names = new HashSet<>();
		for (Method m : methods) {
			if (m.getName().contains("-")) {
				throw new IllegalArgumentException("Illegal method name " + m.getName() + ". Currently no support for methods with hyphens in the name. Please use only [a-zA-z0-9] to name methods!");
			}
			if (names.contains(m.getName())) {
				throw new IllegalArgumentException("Double definition of method " + m.getName());
			}
			names.add(m.getName());
		}
		this.operations = operations;
		this.methods = methods;
		this.checkValidity();
	}

	public void checkValidity() {
		/* does nothing by default */
	}

	public Collection<? extends Operation> getOperations() {
		return Collections.unmodifiableCollection(this.operations);
	}

	public Collection<? extends Method> getMethods() {
		return Collections.unmodifiableCollection(this.methods);
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
