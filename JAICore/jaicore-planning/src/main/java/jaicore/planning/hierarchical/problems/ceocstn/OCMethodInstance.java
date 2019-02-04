package jaicore.planning.hierarchical.problems.ceocstn;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;

@SuppressWarnings("serial")
public class OCMethodInstance extends MethodInstance {

	public OCMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
