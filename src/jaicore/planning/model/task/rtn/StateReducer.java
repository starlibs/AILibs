package jaicore.planning.model.task.rtn;

import jaicore.logic.Monom;

public interface StateReducer {
	public Monom reduce(Monom state);
}
