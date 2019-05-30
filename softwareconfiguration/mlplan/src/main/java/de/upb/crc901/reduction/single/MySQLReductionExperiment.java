package de.upb.crc901.reduction.single;

public class MySQLReductionExperiment {
	private final int id;
	private final ReductionExperiment experiment;

	public MySQLReductionExperiment(int id, ReductionExperiment experiment) {
		super();
		this.id = id;
		this.experiment = experiment;
	}

	public int getId() {
		return id;
	}

	public ReductionExperiment getExperiment() {
		return experiment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((experiment == null) ? 0 : experiment.hashCode());
		result = prime * result + id;
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
		MySQLReductionExperiment other = (MySQLReductionExperiment) obj;
		if (experiment == null) {
			if (other.experiment != null)
				return false;
		} else if (!experiment.equals(other.experiment))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MySQLReductionExperiment [id=" + id + ", experiment=" + experiment + "]";
	}
}
