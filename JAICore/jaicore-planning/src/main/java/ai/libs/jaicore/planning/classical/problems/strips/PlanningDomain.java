package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
		return new HashCodeBuilder().append(this.operations).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PlanningDomain)) {
			return false;
		}
		PlanningDomain other = (PlanningDomain) obj;
		return new EqualsBuilder().append(other.operations, this.operations).isEquals();
	}

}
