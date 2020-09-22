package ai.libs.jaicore.planning.classical.problems.ceoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.classical.problems.ce.CEOperation;

@SuppressWarnings("serial")
public class CEOCOperation extends CEOperation {
	private final List<VariableParam> outputs;

	public CEOCOperation(final String name, final String params, final Monom precondition, final Map<CNFFormula, Monom> addLists, final Map<CNFFormula, Monom> deleteLists, final String  outputs) {
		super(name, params, precondition, addLists, deleteLists);
		this.outputs = Arrays.asList(StringUtil.explode(outputs, ",")).stream().map(s -> new VariableParam(s.trim())).collect(Collectors.toList());
		Collection<VariableParam> allParams = new ArrayList<>();
		allParams.addAll(this.getParams());
		allParams.addAll(this.outputs);
		for (Entry<CNFFormula, Monom> entry : addLists.entrySet()) {
			Collection<VariableParam> missingParamsInPremise = SetUtil.difference(entry.getKey().getVariableParams(), allParams);
			if (!missingParamsInPremise.isEmpty()) {
				throw new IllegalArgumentException("Undeclared parameters in effect premise of operation " + name + ": " + missingParamsInPremise);
			}
			Collection<VariableParam> missingParamsInConclusion = SetUtil.difference(entry.getValue().getVariableParams(), allParams);
			if (!missingParamsInConclusion.isEmpty()) {
				throw new IllegalArgumentException("Undeclared parameters in effect conclusion of operation " + name + ": " + missingParamsInConclusion);
			}
		}
	}

	public CEOCOperation(final String name, final List<VariableParam> params, final Monom precondition, final Map<CNFFormula, Monom> addLists, final Map<CNFFormula, Monom> deleteLists, final List<VariableParam> outputs) {
		super(name, params, precondition, addLists, deleteLists);
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
		if (!(obj instanceof CEOCOperation)) {
			return false;
		}
		CEOCOperation other = (CEOCOperation) obj;
		return new EqualsBuilder().append(other.outputs, this.outputs).isEquals();
	}

	@Override
	public String toString() {
		return "CEOCOperation [name=" + this.getName() + ", params=" + this.getParams() + ", outputs=" + this.outputs + ", precondition=" + this.getPrecondition() + ", addlists=" + this.getAddLists() + ", dellists=" + this.getDeleteLists() + "]";
	}
}
