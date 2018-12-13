package jaicore.planning.classical.algorithms.strips.forward;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.core.Action;

/**
 * We only store the difference to the init state, i.e. what needs to be added or remove from the init state to get the state 
 * 
 * @author fmohr
 *
 */
public class StripsForwardPlanningNode {

	private final Monom add;
	private final Monom del;
	private final Action actionToReachState;

	public StripsForwardPlanningNode(Monom add, Monom del, Action actionToReachState) {
		super();
		assert add != null;
		assert del != null;
		this.add = add;
		this.del = del;
		this.actionToReachState = actionToReachState;
	}

	public Monom getAdd() {
		return add;
	}

	public Monom getDel() {
		return del;
	}
	
	public Monom getStateRelativeToInitState(Monom initState) {
		Monom state = new Monom(initState);
		state.removeAll(del);
		state.addAll(add);
		return state;
	}

	public Action getActionToReachState() {
		return actionToReachState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((add == null) ? 0 : add.hashCode());
		result = prime * result + ((del == null) ? 0 : del.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StripsForwardPlanningNode other = (StripsForwardPlanningNode) obj;
		if (add == null) {
			if (other.add != null)
				return false;
		} else if (!add.equals(other.add))
			return false;
		if (del == null) {
			if (other.del != null)
				return false;
		} else if (!del.equals(other.del))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StripsForwardPlanningNode [addSize=" + add.size() + ", delSize=" + del.size() + ", actionToReachState=" + (actionToReachState != null ? actionToReachState.getEncoding() : null) + "]";
	}
}
