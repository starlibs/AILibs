package jaicore.planning.model.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.basic.ObjectSizeFetcher;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class Action implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2685277085885131650L;
	private final Operation operation;
	private final Map<VariableParam, ConstantParam> grounding;

	public Action(Operation operation, Map<VariableParam, ConstantParam> grounding) {
		super();
		this.operation = operation;
		this.grounding = grounding;
		if (!this.grounding.keySet().containsAll(operation.getParams()))
			throw new IllegalArgumentException(
					"Planning actions must contain a grounding for ALL params of the operation " + operation.getName() + ". Here, op params: " + operation.getParams() + ". Given grounding: " + grounding);
	}

	public List<ConstantParam> getParameters() {
		return operation.getParams().stream().map(p -> grounding.get(p)).collect(Collectors.toList());
	}

	public Map<VariableParam, ConstantParam> getGrounding() {
		return grounding;
	}

	public Operation getOperation() {
		return operation;
	}

	public Monom getPrecondition() {
		return new Monom(operation.getPrecondition(), grounding);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
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
		Action other = (Action) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		return true;
	}

	public String getEncoding() {
		StringBuilder b = new StringBuilder();
		b.append(operation.getName());
		b.append("(");
		List<VariableParam> params = operation.getParams();
		int size = params.size();
		for (int i = 0; i < params.size(); i++) {
			b.append(grounding.get(params.get(i)));
			if (i < size - 1)
				b.append(", ");
		}
		b.append(")");
		return b.toString();
	}

	@Override
	public String toString() {
		return "Action [operation=" + operation + ", grounding=" + grounding + "]";
	}
	
	public long getMemory() {
		return ObjectSizeFetcher.getObjectSize(this) + ObjectSizeFetcher.getObjectSize(grounding);
	}
}
