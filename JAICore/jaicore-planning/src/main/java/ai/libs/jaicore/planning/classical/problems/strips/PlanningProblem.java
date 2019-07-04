package ai.libs.jaicore.planning.classical.problems.strips;

import ai.libs.jaicore.logic.fol.structure.Monom;

public class PlanningProblem {

	private final PlanningDomain domain;
	private final Monom initState;
	private final Monom goalState;
	private final GoalStateFunction goalStateFunction;

	public PlanningProblem(final PlanningDomain domain, final Monom initState, final Monom goalState) {
		this.domain = domain;
		this.initState = initState;
		this.goalState = goalState;
		this.goalStateFunction = s -> s.containsAll(goalState);
	}

	public PlanningProblem(final PlanningDomain domain, final Monom initState, final GoalStateFunction goalStateFunction) {
		super();
		this.domain = domain;
		this.initState = initState;
		this.goalStateFunction = goalStateFunction;
		this.goalState = null;
	}

	public PlanningDomain getDomain() {
		return this.domain;
	}

	public Monom getInitState() {
		return this.initState;
	}

	public GoalStateFunction getGoalStateFunction() {
		return this.goalStateFunction;
	}

	public Monom getGoalState() {
		return this.goalState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
		result = prime * result + ((this.goalStateFunction == null) ? 0 : this.goalStateFunction.hashCode());
		result = prime * result + ((this.initState == null) ? 0 : this.initState.hashCode());
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
		PlanningProblem other = (PlanningProblem) obj;
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		if (this.goalStateFunction == null) {
			if (other.goalStateFunction != null) {
				return false;
			}
		} else if (!this.goalStateFunction.equals(other.goalStateFunction)) {
			return false;
		}
		if (this.initState == null) {
			if (other.initState != null) {
				return false;
			}
		} else if (!this.initState.equals(other.initState)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PlanningProblem [domain=" + this.domain + ", initState=" + this.initState + ", goalStateFunction=" + this.goalStateFunction + "]";
	}
}
