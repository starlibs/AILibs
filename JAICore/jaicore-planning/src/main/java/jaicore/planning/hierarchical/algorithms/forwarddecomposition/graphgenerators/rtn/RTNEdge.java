package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;

public class RTNEdge {

	private final Map<ConstantParam, ConstantParam> contextRecreator;
	private final MethodInstance methodInstance;
	private final CEOCAction appliedAction;

	public RTNEdge(Map<ConstantParam, ConstantParam> contextRecreator, MethodInstance methodInstance, CEOCAction appliedAction) {
		super();
		this.contextRecreator = contextRecreator;
		this.methodInstance = methodInstance;
		this.appliedAction = appliedAction;
	}

	public Map<ConstantParam, ConstantParam> getContextRecreator() {
		return contextRecreator;
	}

	public MethodInstance getMethodInstance() {
		return methodInstance;
	}

	public CEOCAction getAppliedAction() {
		return appliedAction;
	}

}
