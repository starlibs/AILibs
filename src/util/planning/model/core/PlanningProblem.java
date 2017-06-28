package util.planning.model.core;

import util.logic.Monom;

public class PlanningProblem {

	private final PlanningDomain domain;
	private final Monom initState, goalState;

	public PlanningProblem(PlanningDomain domain, Monom initState, Monom goalState) {
		super();
		this.domain = domain;
		this.initState = initState;
		this.goalState = goalState;
	}

	public PlanningDomain getDomain() {
		return domain;
	}

	public Monom getInitState() {
		return initState;
	}

	public Monom getGoalState() {
		return goalState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((goalState == null) ? 0 : goalState.hashCode());
		result = prime * result + ((initState == null) ? 0 : initState.hashCode());
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
		PlanningProblem other = (PlanningProblem) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (goalState == null) {
			if (other.goalState != null)
				return false;
		} else if (!goalState.equals(other.goalState))
			return false;
		if (initState == null) {
			if (other.initState != null)
				return false;
		} else if (!initState.equals(other.initState))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlanningProblem [domain=" + domain + ", initState=" + initState + ", goalState=" + goalState + "]";
	}
}
