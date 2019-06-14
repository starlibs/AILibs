package ai.libs.reduction.single;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.experiment == null) ? 0 : this.experiment.hashCode());
		result = prime * result + this.id;
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
		MySQLReductionExperiment other = (MySQLReductionExperiment) obj;
		if (this.experiment == null) {
			if (other.experiment != null) {
				return false;
			}
		} else if (!this.experiment.equals(other.experiment)) {
			return false;
		}
		return this.id == other.id;
	}

	@Override
	public String toString() {
		return "MySQLReductionExperiment [id=" + this.id + ", experiment=" + this.experiment + "]";
	}
}
