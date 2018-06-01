package jaicore.planning.graphgenerators.task.tfd;

import java.io.Serializable;
import java.util.List;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class TFDRestProblem implements Serializable {

	private static final long serialVersionUID = 6946349883053172033L;
	private final Monom state;
	private final List<Literal> remainingTasks;

	public TFDRestProblem(Monom state, List<Literal> remainingTasks) {
		super();
		this.state = state;
		this.remainingTasks = remainingTasks;
	}

	public Monom getState() {
		return state;
	}

	public List<Literal> getRemainingTasks() {
		return remainingTasks;
	}
	
	@Override
	public String toString() {
		return "TFDRestProblem [state=" + state + ", remainingTasks=" + remainingTasks + "]";
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
		TFDRestProblem other = (TFDRestProblem) obj;
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
}
