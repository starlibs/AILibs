package jaicore.planning.model.core;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.strips.GoalStateFunction;

public class PlanningProblem {

	private final PlanningDomain domain;
	private final Monom initState;
	private final Monom goalState;
	private final GoalStateFunction goalStateFunction;

	public PlanningProblem(PlanningDomain domain, Monom initState, Monom goalState) {
		this.domain = domain;
		this.initState = initState;
		this.goalState = goalState;
		this.goalStateFunction = s -> s.containsAll(goalState);
	}
	
	public PlanningProblem(PlanningDomain domain, Monom initState, GoalStateFunction goalStateFunction) {
		super();
		this.domain = domain;
		this.initState = initState;
		this.goalStateFunction = goalStateFunction;
		this.goalState = null;
	}

	public PlanningDomain getDomain() {
		return domain;
	}

	public Monom getInitState() {
		return initState;
	}

	public GoalStateFunction getGoalStateFunction() {
		return goalStateFunction;
	}

	public Monom getGoalState() {
		return goalState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((goalStateFunction == null) ? 0 : goalStateFunction.hashCode());
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
		if (goalStateFunction == null) {
			if (other.goalStateFunction != null)
				return false;
		} else if (!goalStateFunction.equals(other.goalStateFunction))
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
		return "PlanningProblem [domain=" + domain + ", initState=" + initState + ", goalStateFunction="
				+ goalStateFunction + "]";
	}
}
