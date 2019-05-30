package jaicore.planning.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.strips.Operation;

public class Action implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -2685277085885131650L;
	private final Operation operation;
	private final Map<VariableParam, ConstantParam> grounding;

	public Action(final Operation operation, final Map<VariableParam, ConstantParam> grounding) {
		super();
		this.operation = operation;
		this.grounding = grounding;
		if (!this.grounding.keySet().containsAll(operation.getParams())) {
			throw new IllegalArgumentException("Planning actions must contain a grounding for ALL params of the operation " + operation.getName() + ". Here, op params: " + operation.getParams() + ". Given grounding: " + grounding);
		}
	}

	public List<ConstantParam> getParameters() {
		return this.operation.getParams().stream().map(p -> this.grounding.get(p)).collect(Collectors.toList());
	}

	public Map<VariableParam, ConstantParam> getGrounding() {
		return this.grounding;
	}

	public Operation getOperation() {
		return this.operation;
	}

	public Monom getPrecondition() {
		return new Monom(this.operation.getPrecondition(), this.grounding);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.grounding == null) ? 0 : this.grounding.hashCode());
		result = prime * result + ((this.operation == null) ? 0 : this.operation.hashCode());
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
		Action other = (Action) obj;
		if (this.grounding == null) {
			if (other.grounding != null) {
				return false;
			}
		} else if (!this.grounding.equals(other.grounding)) {
			return false;
		}
		if (this.operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!this.operation.equals(other.operation)) {
			return false;
		}
		return true;
	}

	public String getEncoding() {
		StringBuilder b = new StringBuilder();
		b.append(this.operation.getName());
		b.append("(");
		List<VariableParam> params = this.operation.getParams();
		int size = params.size();
		for (int i = 0; i < params.size(); i++) {
			b.append(this.grounding.get(params.get(i)));
			if (i < size - 1) {
				b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("operation", this.operation);
		fields.put("grounding", this.grounding);
		return ToJSONStringUtil.toJSONString(fields);
	}

}
