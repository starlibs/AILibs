package ai.libs.jaicore.planning.hierarchical.problems.ceocstn;

import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.planning.hierarchical.problems.stn.MethodInstance;

@SuppressWarnings("serial")
public class OCMethodInstance extends MethodInstance {

	public OCMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
