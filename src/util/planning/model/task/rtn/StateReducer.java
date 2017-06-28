package util.planning.model.task.rtn;

import util.logic.Monom;

public interface StateReducer {
	public Monom reduce(Monom state);
}
