package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.List;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class RTNNode {

	private static int counter = 0;
	private final int id = counter ++;
	
	private final boolean andNode;
	private Monom state;
	private List<Literal> remainingTasks;

	public RTNNode(boolean andNode, Monom state, List<Literal> remainingTasks) {
		super();
		this.andNode = andNode;
		this.state = state;
		this.remainingTasks = remainingTasks;
	}

	public Monom getState() {
		return state;
	}

	public void setState(Monom state) {
		this.state = state;
	}

	public List<Literal> getRemainingTasks() {
		return remainingTasks;
	}

	public void setRemainingTasks(List<Literal> remainingTasks) {
		this.remainingTasks = remainingTasks;
	}

	public boolean isAndNode() {
		return andNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((remainingTasks == null) ? 0 : remainingTasks.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		RTNNode other = (RTNNode) obj;
		if (remainingTasks == null) {
			if (other.remainingTasks != null)
				return false;
		} else if (!remainingTasks.equals(other.remainingTasks))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RTNNode [id=" + id + ", state=" + state + ", remainingTasks=" + remainingTasks + "]";
	}

	public int getId() {
		return id;
	}
}
