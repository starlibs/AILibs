package ai.libs.jaicore.planning.classical.problems.strips;

import ai.libs.jaicore.logic.fol.structure.Monom;

public interface GoalStateFunction {
	public boolean isGoalState(Monom state);
}
