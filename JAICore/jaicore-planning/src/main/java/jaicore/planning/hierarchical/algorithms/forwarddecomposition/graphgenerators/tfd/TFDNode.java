package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;

public class TFDNode implements Serializable {

	private static final long serialVersionUID = 7710905829501897491L;

	private TFDRestProblem problem;
	private final MethodInstance appliedMethodInstance;
	private final Action appliedAction;
	private final boolean isGoal;

	public TFDNode(final Monom initialState, final List<Literal> remainingTasks) {
		this(initialState, remainingTasks, null, null);
	}

	public TFDNode(final MethodInstance appliedMethodInstance, final boolean isGoal) {
		super();
		this.problem = null;
		this.appliedMethodInstance = appliedMethodInstance;
		this.appliedAction = null;
		this.isGoal = isGoal;
	}

	public TFDNode(final Action appliedAction, final boolean isGoal) {
		super();
		this.problem = null;
		this.appliedAction = appliedAction;
		this.appliedMethodInstance = null;
		this.isGoal = isGoal;
	}

	public TFDNode(final Monom state, final List<Literal> remainingTasks, final MethodInstance appliedMethodInstance, final Action appliedAction) {
		super();
		this.problem = new TFDRestProblem(state, remainingTasks);
		this.appliedMethodInstance = appliedMethodInstance;
		this.appliedAction = appliedAction;
		this.isGoal = remainingTasks.isEmpty();
	}

	public TFDRestProblem getProblem() {
		return this.problem;
	}

	public Monom getState() {
		return this.problem.getState();
	}

	public List<Literal> getRemainingTasks() {
		return this.problem.getRemainingTasks();
	}

	public Action getAppliedAction() {
		return this.appliedAction;
	}

	public MethodInstance getAppliedMethodInstance() {
		return this.appliedMethodInstance;
	}

	public boolean isGoal() {
		return this.isGoal;
	}

	public void clear() {
		this.problem = null;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("isGoal", this.isGoal);
		fields.put("problem", this.problem);
		fields.put("appliedMethodInstance", this.appliedMethodInstance);
		fields.put("appliedAction", this.appliedAction);
		return ToJSONStringUtil.toJSONString("TFDNode", fields);
		// return "TFDNode [problem=" + this.problem + ", appliedMethodInstance=" + this.appliedMethodInstance + ", appliedAction=" + this.appliedAction + ", isGoal=" + this.isGoal + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.appliedAction == null) ? 0 : this.appliedAction.hashCode());
		result = prime * result + ((this.appliedMethodInstance == null) ? 0 : this.appliedMethodInstance.hashCode());
		result = prime * result + (this.isGoal ? 1231 : 1237);
		result = prime * result + ((this.problem == null) ? 0 : this.problem.hashCode());
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
		TFDNode other = (TFDNode) obj;
		if (this.appliedAction == null) {
			if (other.appliedAction != null) {
				return false;
			}
		} else if (!this.appliedAction.equals(other.appliedAction)) {
			return false;
		}
		if (this.appliedMethodInstance == null) {
			if (other.appliedMethodInstance != null) {
				return false;
			}
		} else if (!this.appliedMethodInstance.equals(other.appliedMethodInstance)) {
			return false;
		}
		if (this.isGoal != other.isGoal) {
			return false;
		}
		if (this.problem == null) {
			if (other.problem != null) {
				return false;
			}
		} else if (!this.problem.equals(other.problem)) {
			return false;
		}
		return true;
	}
}
