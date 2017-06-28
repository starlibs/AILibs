package util.planning.model.task.stn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import util.logic.ConstantParam;
import util.logic.Literal;
import util.logic.Monom;
import util.logic.VariableParam;

public class MethodInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8957990820135975139L;
	private final Method method;
	private final Map<VariableParam, ConstantParam> grounding;
	private final Monom precondition;

	public MethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super();
		this.method = method;
		this.grounding = grounding;
		if (!this.grounding.keySet().containsAll(method.getParameters()))
			throw new IllegalArgumentException("Planning Method instances must contain a grounding for ALL params of the method. Here, method (" + method.getName() + ") params: " + method.getParameters()
					+ ". Given grounding: " + grounding);
		precondition = new Monom(method.getPrecondition(), grounding);
	}

	public Method getMethod() {
		return method;
	}

	public Map<VariableParam, ConstantParam> getGrounding() {
		return grounding;
	}

	public Monom getPrecondition() {
		return precondition;
	}

	public List<ConstantParam> getParameters() {
		return method.getParameters().stream().map(p -> grounding.get(p)).collect(Collectors.toList());
	}

	public TaskNetwork getNetwork() {
		TaskNetwork instanceNetwork = new TaskNetwork();
		TaskNetwork methodNetwork = getMethod().getNetwork();
		Map<Literal, Literal> correspondence = new HashMap<>();
		for (Literal task : methodNetwork.getItems()) {
			Literal groundTask = new Literal(task, grounding);
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
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodInstance other = (MethodInstance) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodInstance [method=" + method + ", grounding=" + grounding + ", precondition=" + precondition + "]";
	}

	public String getEncoding() {
		StringBuilder b = new StringBuilder();
		b.append(method.getName());
		b.append("(");
		List<VariableParam> params = method.getParameters();
		int size = params.size();
		for (int i = 0; i < params.size(); i++) {
			b.append(grounding.get(params.get(i)));
			if (i < size - 1)
				b.append(", ");
		}
		b.append(")");
		return b.toString();
	}
}
