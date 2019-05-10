package jaicore.planning.model.task.rtn;

import jaicore.logic.fol.structure.Monom;

public interface StateReducer {
	public Monom reduce(Monom state);
}
