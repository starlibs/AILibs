package ai.libs.jaicore.problems.scheduling.openshop;

/**
 **/
public class Operation {
	private final String name;
	private final int processTime; // processTime. The processing time of the operation valid integer:
	private final int status; // the state in which the machine must be to conduct this operation
	private final Job job; // the job this operation belongs to
	private final Workcenter workcenter; // work center in which this operation needs to be realized

	Operation(final String name, final int processTime, final int status, final Job job, final Workcenter workcenter) {
		super();
		this.name = name;
		this.processTime = processTime;
		this.status = status;
		this.job = job;
		this.workcenter = workcenter;
		job.addOperation(this);
	}

	public String getName() {
		return this.name;
	}

	public int getProcessTime() {
		return this.processTime;
	}

	public int getStatus() {
		return this.status;
	}

	public Workcenter getWorkcenter() {
		return this.workcenter;
	}

	public Job getJob() {
		return this.job;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.job == null) ? 0 : this.job.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + this.processTime;
		result = prime * result + this.status;
		result = prime * result + ((this.workcenter == null) ? 0 : this.workcenter.hashCode());
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
		if (this.job == null) {
			if (other.job != null) {
				return false;
			}
		} else if (!this.job.equals(other.job)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.processTime != other.processTime) {
			return false;
		}
		if (this.status != other.status) {
			return false;
		}
		if (this.workcenter == null) {
			if (other.workcenter != null) {
				return false;
			}
		} else if (!this.workcenter.equals(other.workcenter)) {
			return false;
		}
		return true;
	}
}
