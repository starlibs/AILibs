package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
		return new HashCodeBuilder().append(this.state).append(this.remainingTasks).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RTNNode)) {
			return false;
		}
		RTNNode other = (RTNNode) obj;
		return new EqualsBuilder().append(other.state, this.state).append(other.remainingTasks, this.remainingTasks).isEquals();

	}

	@Override
	public String toString() {
		return "RTNNode [id=" + this.id + ", state=" + this.state + ", remainingTasks=" + this.remainingTasks + "]";
	}

	public int getId() {
		return this.id;
	}
}
