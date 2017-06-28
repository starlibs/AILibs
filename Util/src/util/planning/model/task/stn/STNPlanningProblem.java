package util.planning.model.task.stn;

import util.logic.CNFFormula;
import util.logic.Monom;

public class STNPlanningProblem {

	private final STNPlanningDomain domain;
	
	private final CNFFormula knowledge;
	private final Monom init;

	private final TaskNetwork network;

	public STNPlanningProblem(STNPlanningDomain domain, CNFFormula knowledge, Monom init, TaskNetwork network) {
		super();
		this.domain = domain;
		this.knowledge = knowledge;
		this.init = init;
		this.network = network;
	}

	public STNPlanningDomain getDomain() {
		return domain;
	}
	
	public CNFFormula getKnowledge() {
		return knowledge;
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
		result = prime * result + ((knowledge == null) ? 0 : knowledge.hashCode());
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
		STNPlanningProblem other = (STNPlanningProblem) obj;
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
		if (knowledge == null) {
			if (other.knowledge != null)
				return false;
		} else if (!knowledge.equals(other.knowledge))
			return false;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		return true;
	}
}
