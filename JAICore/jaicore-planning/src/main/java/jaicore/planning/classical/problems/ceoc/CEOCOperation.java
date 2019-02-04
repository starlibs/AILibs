package jaicore.planning.classical.problems.ceoc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.basic.StringUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.ce.CEOperation;

@SuppressWarnings("serial")
public class CEOCOperation extends CEOperation {
	
	private final List<VariableParam> outputs;

	public CEOCOperation(String name, String params, Monom precondition, Map<CNFFormula, Monom> addLists, Map<CNFFormula, Monom> deleteLists, String  outputs) {
		super(name, params, precondition, addLists, deleteLists);
		this.outputs = Arrays.asList(StringUtil.explode(outputs, ",")).stream().map(s -> new VariableParam(s.trim())).collect(Collectors.toList());
	}
	
	public CEOCOperation(String name, List<VariableParam> params, Monom precondition, Map<CNFFormula, Monom> addLists, Map<CNFFormula, Monom> deleteLists, List<VariableParam> outputs) {
		super(name, params, precondition, addLists, deleteLists);
		this.outputs = outputs;
	}

	public List<VariableParam> getOutputs() {
		return outputs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CEOCOperation other = (CEOCOperation) obj;
		if (outputs == null) {
			if (other.outputs != null)
				return false;
		} else if (!outputs.equals(other.outputs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CEOCOperation [name=" + getName() + ", params=" + getParams() + ", outputs=" + outputs + ", precondition=" + getPrecondition() + ", addlists=" + getAddLists() + ", dellists=" + getDeleteLists() + "]";
	}
}
