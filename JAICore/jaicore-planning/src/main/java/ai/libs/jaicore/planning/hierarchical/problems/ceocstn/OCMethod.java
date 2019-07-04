package ai.libs.jaicore.planning.hierarchical.problems.ceocstn;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
public class OCMethod extends Method {

	private final List<VariableParam> outputs;

	public OCMethod(final String name, final List<VariableParam> parameters, final Literal task, final Monom precondition, final TaskNetwork network, final boolean lonely, final List<VariableParam> outputs) {
		super(name, parameters, task, precondition, network, lonely);
		this.outputs = outputs;
	}

	public List<VariableParam> getOutputs() {
		return this.outputs;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(super.hashCode()).append(this.outputs).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof OCMethod)) {
			return false;
		}
		OCMethod other = (OCMethod) obj;
		return new EqualsBuilder().append(this.outputs, other.outputs).isEquals();
	}

}
