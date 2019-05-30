package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class TFDRestProblem implements Serializable {

	private static final long serialVersionUID = 6946349883053172033L;
	private final Monom state;
	private final List<Literal> remainingTasks;

	public TFDRestProblem(final Monom state, final List<Literal> remainingTasks) {
		super();
		this.state = state;
		this.remainingTasks = remainingTasks;
	}

	public Monom getState() {
		return this.state;
	}

	public List<Literal> getRemainingTasks() {
		return this.remainingTasks;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("state", this.state);
		fields.put("remainingTasks", remainingTasks);
		return ToJSONStringUtil.toJSONString(fields);
//			return "TFDRestProblem [state=" + this.state + ", remainingTasks=" + this.remainingTasks + "]";
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
		TFDRestProblem other = (TFDRestProblem) obj;
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
}
