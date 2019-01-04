package jaicore.planning.classical.problems.strips;

import java.util.Collection;

public class PlanningDomain {

	private final Collection<Operation> operations;

	public PlanningDomain(Collection<Operation> operations) {
		super();
		this.operations = operations;
	}

	public Collection<Operation> getOperations() {
		return operations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		PlanningDomain other = (PlanningDomain) obj;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}

}
