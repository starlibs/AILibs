package ai.libs.jaicore.planning.hierarchical.problems.rtn;

import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
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
