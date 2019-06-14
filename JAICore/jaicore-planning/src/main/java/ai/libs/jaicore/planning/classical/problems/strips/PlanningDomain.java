package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.Collection;

public class PlanningDomain {

	private final Collection<Operation> operations;

	public PlanningDomain(final Collection<Operation> operations) {
		super();
		this.operations = operations;
	}

	public Collection<Operation> getOperations() {
		return this.operations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		PlanningDomain other = (PlanningDomain) obj;
		if (this.operations == null) {
			if (other.operations != null) {
				return false;
			}
		} else if (!this.operations.equals(other.operations)) {
			return false;
		}
		return true;
	}

}
