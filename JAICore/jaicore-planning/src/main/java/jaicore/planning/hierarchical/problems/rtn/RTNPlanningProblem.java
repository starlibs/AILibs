package jaicore.planning.hierarchical.problems.rtn;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class RTNPlanningProblem {

	private final RTNPlanningDomain domain;

	private final Monom init;

	private final TaskNetwork network;

	public RTNPlanningProblem(RTNPlanningDomain domain, Monom init, TaskNetwork network) {
		super();
		this.domain = domain;
		this.init = init;
		this.network = network;
	}

	public RTNPlanningDomain getDomain() {
		return domain;
	}

	public Monom getInit() {
		return init;
	}

	public TaskNetwork getNetwork() {
		return network;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((init == null) ? 0 : init.hashCode());
		result = prime * result + ((network == null) ? 0 : network.hashCode());
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
		RTNPlanningProblem other = (RTNPlanningProblem) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (init == null) {
			if (other.init != null)
				return false;
		} else if (!init.equals(other.init))
			return false;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		return true;
	}
	
}
