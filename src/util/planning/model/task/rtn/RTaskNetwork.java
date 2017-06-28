package util.planning.model.task.rtn;

import java.util.Map;

import util.logic.Literal;
import util.planning.model.task.stn.TaskNetwork;

public class RTaskNetwork extends TaskNetwork {

	private final Map<Literal,StateReducer> reducers;

	public RTaskNetwork(TaskNetwork network, Map<Literal, StateReducer> reducers) {
		super(network);
		this.reducers = reducers;
	}

	public Map<Literal, StateReducer> getReducers() {
		return reducers;
	}
}
