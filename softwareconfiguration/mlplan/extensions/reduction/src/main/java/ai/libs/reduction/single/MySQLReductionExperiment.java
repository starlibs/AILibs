package ai.libs.reduction.single;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class MySQLReductionExperiment {
	private final int id;
	private final ReductionExperiment experiment;

	public MySQLReductionExperiment(final int id, final ReductionExperiment experiment) {
		super();
		this.id = id;
		this.experiment = experiment;
	}

	public int getId() {
		return this.id;
	}

	public ReductionExperiment getExperiment() {
		return this.experiment;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.experiment).append(this.id).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof MySQLReductionExperiment)) {
			return false;
		}
		MySQLReductionExperiment other = (MySQLReductionExperiment)obj;
		return new EqualsBuilder().append(other.id, this.id).append(other.experiment, this.experiment).isEquals();
	}

	@Override
	public String toString() {
		return "MySQLReductionExperiment [id=" + this.id + ", experiment=" + this.experiment + "]";
	}
}
