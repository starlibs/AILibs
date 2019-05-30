package jaicore.planning.hierarchical.problems.stn;

import java.io.Serializable;
import java.util.List;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class Method implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7744180734251229160L;
	private final String name;
	private final List<VariableParam> parameters;
	private final Literal task;
	private final Monom precondition;
	private final TaskNetwork network;
	private final boolean lonely;

	public Method(String name, List<VariableParam> parameters, Literal task, Monom precondition, TaskNetwork network, boolean lonely) {
		super();
		this.name = name;
		this.parameters = parameters;
		this.task = task;
		this.precondition = precondition;
		this.network = network;
		this.lonely = lonely;
		assert doAllParamsInNetworkOccurInParameterList() : "Invalid method instantiation for " + name + ". There are parameters in the task network that do not occur in the parameter list.";
	}
	
	private boolean doAllParamsInNetworkOccurInParameterList() {
		for (Literal l : this.network.getItems()) {
			if (!SetUtil.difference(l.getVariableParams(), this.parameters).isEmpty())
				return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public List<VariableParam> getParameters() {
		return parameters;
	}

	public Literal getTask() {
		return task;
	}

	public Monom getPrecondition() {
		return precondition;
	}

	public TaskNetwork getNetwork() {
		return network;
	}

	public boolean isLonely() {
		return lonely;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((network == null) ? 0 : network.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((precondition == null) ? 0 : precondition.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		Method other = (Method) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (precondition == null) {
			if (other.precondition != null)
				return false;
		} else if (!precondition.equals(other.precondition))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Method [name=" + name + ", parameters=" + parameters + ", task=" + task + ", precondition=" + precondition + ", network=" + network + ", lonely=" + lonely + "]";
	}

}
