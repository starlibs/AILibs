package jaicore.planning.hierarchical.problems.stn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;
import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class MethodInstance implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8957990820135975139L;
	private final Method method;
	private final Map<VariableParam, ConstantParam> grounding;
	private final Monom precondition;

	public MethodInstance(final Method method, final Map<VariableParam, ConstantParam> grounding) {
		super();
		this.method = method;
		this.grounding = grounding;
		if (!this.grounding.keySet().containsAll(method.getParameters())) {
			throw new IllegalArgumentException("Planning Method instances must contain a grounding for ALL params of the method. Here, method (" + method.getName() + ") params: " + method.getParameters() + ". Params missing: "
					+ SetUtil.difference(method.getParameters(), this.grounding.keySet()));
		}
		this.precondition = new Monom(method.getPrecondition(), grounding);
	}

	public Method getMethod() {
		return this.method;
	}

	public Map<VariableParam, ConstantParam> getGrounding() {
		return this.grounding;
	}

	public Monom getPrecondition() {
		return this.precondition;
	}

	public List<ConstantParam> getParameters() {
		return this.method.getParameters().stream().map(p -> this.grounding.get(p)).collect(Collectors.toList());
	}

	public TaskNetwork getNetwork() {
		TaskNetwork instanceNetwork = new TaskNetwork();
		TaskNetwork methodNetwork = this.getMethod().getNetwork();
		Map<Literal, Literal> correspondence = new HashMap<>();
		for (Literal task : methodNetwork.getItems()) {
			Literal groundTask = new Literal(task, this.grounding);
			correspondence.put(task, groundTask);
			instanceNetwork.addItem(groundTask);

		}
		for (Literal task : methodNetwork.getItems()) {
			for (Literal succ : methodNetwork.getSuccessors(task)) {
				instanceNetwork.addEdge(correspondence.get(task), correspondence.get(succ));
			}
		}
		return instanceNetwork;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.method == null) ? 0 : this.method.hashCode());
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
		MethodInstance other = (MethodInstance) obj;
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("method", this.method);
		fields.put("grounding", this.grounding);
		fields.put("precondition", this.precondition);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
		// return "MethodInstance [method=" + this.method + ", grounding=" + this.grounding + ", precondition=" + this.precondition + "]";
	}

	public String getEncoding() {
		StringBuilder b = new StringBuilder();
		b.append(this.method.getName());
		b.append("(");
		List<VariableParam> params = this.method.getParameters();
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
}
