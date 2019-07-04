package ai.libs.jaicore.planning.hierarchical.problems.rtn;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class RTNPlanningProblem {

	private final RTNPlanningDomain domain;

	private final Monom init;

	private final TaskNetwork network;

	public RTNPlanningProblem(final RTNPlanningDomain domain, final Monom init, final TaskNetwork network) {
		super();
		this.domain = domain;
		this.init = init;
		this.network = network;
	}

	public RTNPlanningDomain getDomain() {
		return this.domain;
	}

	public Monom getInit() {
		return this.init;
	}

	public TaskNetwork getNetwork() {
		return this.network;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
		result = prime * result + ((this.init == null) ? 0 : this.init.hashCode());
		result = prime * result + ((this.network == null) ? 0 : this.network.hashCode());
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
		RTNPlanningProblem other = (RTNPlanningProblem) obj;
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		if (this.init == null) {
			if (other.init != null) {
				return false;
			}
		} else if (!this.init.equals(other.init)) {
			return false;
		}
		if (this.network == null) {
			if (other.network != null) {
				return false;
			}
		} else if (!this.network.equals(other.network)) {
			return false;
		}
		return true;
	}

}
