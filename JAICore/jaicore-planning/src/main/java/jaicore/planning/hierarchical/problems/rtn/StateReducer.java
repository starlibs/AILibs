package jaicore.planning.hierarchical.problems.rtn;

import jaicore.logic.fol.structure.Monom;

public interface StateReducer {
	public Monom reduce(Monom state);
}
