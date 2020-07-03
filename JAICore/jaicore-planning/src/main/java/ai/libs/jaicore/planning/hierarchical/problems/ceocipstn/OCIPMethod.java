package ai.libs.jaicore.planning.hierarchical.problems.ceocipstn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
public class OCIPMethod extends OCMethod {

	private final Monom evaluablePrecondition;

	public OCIPMethod(final String name, final String parameters, final Literal task, final Monom precondition, final TaskNetwork network, final boolean lonely, final String outputs, final Monom evaluablePrecondition) {
		this(name, Arrays.asList(StringUtil.explode(parameters, ",")).stream().map(s -> new VariableParam(s.trim())).collect(Collectors.toList()), task, precondition, network, lonely,
				Arrays.asList(StringUtil.explode(outputs, ",")).stream().map(s -> new VariableParam(s.trim())).collect(Collectors.toList()), evaluablePrecondition);
	}

	public OCIPMethod(final String name, final List<VariableParam> parameters, final Literal task, final Monom precondition, final TaskNetwork network, final boolean lonely, final List<VariableParam> outputs, final Monom evaluablePrecondition) {
		super(name, parameters, task, precondition, network, lonely, outputs);
		this.evaluablePrecondition = evaluablePrecondition;
	}

	public Monom getEvaluablePrecondition() {
		return this.evaluablePrecondition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.evaluablePrecondition == null) ? 0 : this.evaluablePrecondition.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		OCIPMethod other = (OCIPMethod) obj;
		if (this.evaluablePrecondition == null) {
			if (other.evaluablePrecondition != null) {
				return false;
			}
		} else if (!this.evaluablePrecondition.equals(other.evaluablePrecondition)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OCIPMethod [" + super.toString() + ", evaluablePrecondition=" + this.evaluablePrecondition + "]";
	}
}
