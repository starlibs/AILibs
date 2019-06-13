package ai.libs.jaicore.planning.hierarchical.problems.rtn;

import ai.libs.jaicore.logic.fol.structure.Monom;

public interface StateReducer {
	public Monom reduce(Monom state);
}
