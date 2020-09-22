package ai.libs.jaicore.planning.hierarchical.problems.stn;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;

public class Method implements Serializable {

	private static final long serialVersionUID = 704153871624796863L;
	private final String name;
	private final List<VariableParam> parameters;
	private final Literal task;
	private final Monom precondition;
	private final TaskNetwork network;
	private final boolean lonely;

	public Method(final String name, final List<VariableParam> parameters, final Literal task, final Monom precondition, final TaskNetwork network, final boolean lonely) {
		super();
		this.name = name;
		this.parameters = parameters;
		this.task = task;
		this.precondition = precondition;
		this.network = network;
		this.lonely = lonely;
		if (!this.doAllParamsPreconditionOccurInParameterList()) {
			Set<VariableParam> undeclaredVars = new HashSet<>();
			for (Literal l : precondition) {
				undeclaredVars.addAll(SetUtil.difference(l.getVariableParams(), this.parameters));
			}
			throw new IllegalArgumentException("Invalid method " + name + ". There are parameters in the precondition that do not occur in the parameter list:" + undeclaredVars.stream().map(v -> "\n\t- " + v.getName()).collect(Collectors.joining()));
		}
		if (!this.doAllParamsInNetworkOccurInParameterList()) {
			Set<VariableParam> undeclaredVars = new HashSet<>();
			for (Literal l : this.network.getItems()) {
				undeclaredVars.addAll(SetUtil.difference(l.getVariableParams(), this.parameters));
			}
			throw new IllegalArgumentException("Invalid method " + name + ". There are parameters in the task network that do not occur in the parameter list:" + undeclaredVars.stream().map(v -> "\n\t- " + v.getName()).collect(Collectors.joining()));
		}
	}

	private boolean doAllParamsInNetworkOccurInParameterList() {
		for (Literal l : this.network.getItems()) {
			if (!SetUtil.difference(l.getVariableParams(), this.parameters).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean doAllParamsPreconditionOccurInParameterList() {
		for (Literal l : this.precondition) {
			if (!SetUtil.difference(l.getVariableParams(), this.parameters).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public String getName() {
		return this.name;
	}

	public List<VariableParam> getParameters() {
		return this.parameters;
	}

	public Literal getTask() {
		return this.task;
	}

	public Monom getPrecondition() {
		return this.precondition;
	}

	public TaskNetwork getNetwork() {
		return this.network;
	}

	public boolean isLonely() {
		return this.lonely;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.network == null) ? 0 : this.network.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		result = prime * result + ((this.precondition == null) ? 0 : this.precondition.hashCode());
		result = prime * result + ((this.task == null) ? 0 : this.task.hashCode());
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
		Method other = (Method) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.network == null) {
			if (other.network != null) {
				return false;
			}
		} else if (!this.network.equals(other.network)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		if (this.precondition == null) {
			if (other.precondition != null) {
				return false;
			}
		} else if (!this.precondition.equals(other.precondition)) {
			return false;
		}
		if (this.task == null) {
			if (other.task != null) {
				return false;
			}
		} else if (!this.task.equals(other.task)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Method [name=" + this.name + ", parameters=" + this.parameters + ", task=" + this.task + ", precondition=" + this.precondition + ", network=" + this.network.getLineBasedStringRepresentation() + ", lonely=" + this.lonely + "]";
	}

}
