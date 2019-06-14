package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.List;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;

public class RTNNode {

	private static int counter = 0;
	private final int id = counter ++;

	private final boolean andNode;
	private Monom state;
	private List<Literal> remainingTasks;

	public RTNNode(final boolean andNode, final Monom state, final List<Literal> remainingTasks) {
		super();
		this.andNode = andNode;
		this.state = state;
		this.remainingTasks = remainingTasks;
	}

	public Monom getState() {
		return this.state;
	}

	public void setState(final Monom state) {
		this.state = state;
	}

	public List<Literal> getRemainingTasks() {
		return this.remainingTasks;
	}

	public void setRemainingTasks(final List<Literal> remainingTasks) {
		this.remainingTasks = remainingTasks;
	}

	public boolean isAndNode() {
		return this.andNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.remainingTasks == null) ? 0 : this.remainingTasks.hashCode());
		result = prime * result + ((this.state == null) ? 0 : this.state.hashCode());
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
		RTNNode other = (RTNNode) obj;
		if (this.remainingTasks == null) {
			if (other.remainingTasks != null) {
				return false;
			}
		} else if (!this.remainingTasks.equals(other.remainingTasks)) {
			return false;
		}
		if (this.state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!this.state.equals(other.state)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RTNNode [id=" + this.id + ", state=" + this.state + ", remainingTasks=" + this.remainingTasks + "]";
	}

	public int getId() {
		return this.id;
	}
}
