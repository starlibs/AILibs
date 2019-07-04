package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.core.Action;

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

	public StripsForwardPlanningNode(final Monom add, final Monom del, final Action actionToReachState) {
		super();
		if (add == null) {
			throw new IllegalArgumentException("Add list must not be NULL");
		}
		if (del == null) {
			throw new IllegalArgumentException("Del list must not be NULL");
		}
		this.add = add;
		this.del = del;
		this.actionToReachState = actionToReachState;
	}

	public Monom getAdd() {
		return this.add;
	}

	public Monom getDel() {
		return this.del;
	}

	public Monom getStateRelativeToInitState(final Monom initState) {
		Monom state = new Monom(initState);
		state.removeAll(this.del);
		state.addAll(this.add);
		return state;
	}

	public Action getActionToReachState() {
		return this.actionToReachState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.add == null) ? 0 : this.add.hashCode());
		result = prime * result + ((this.del == null) ? 0 : this.del.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		StripsForwardPlanningNode other = (StripsForwardPlanningNode) obj;
		if (this.add == null) {
			if (other.add != null) {
				return false;
			}
		} else if (!this.add.equals(other.add)) {
			return false;
		}
		if (this.del == null) {
			if (other.del != null) {
				return false;
			}
		} else if (!this.del.equals(other.del)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StripsForwardPlanningNode [addSize=" + this.add.size() + ", delSize=" + this.del.size() + ", actionToReachState=" + (this.actionToReachState != null ? this.actionToReachState.getEncoding() : null) + "]";
	}
}
