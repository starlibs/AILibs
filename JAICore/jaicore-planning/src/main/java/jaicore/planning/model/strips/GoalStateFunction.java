package jaicore.planning.model.strips;

import jaicore.logic.fol.structure.Monom;

public interface GoalStateFunction {
	public boolean isGoalState(Monom state);
}
