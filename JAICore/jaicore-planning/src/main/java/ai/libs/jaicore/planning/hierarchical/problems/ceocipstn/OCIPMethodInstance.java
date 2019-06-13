package ai.libs.jaicore.planning.hierarchical.problems.ceocipstn;

import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.OCMethodInstance;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;

@SuppressWarnings("serial")
public class OCIPMethodInstance extends OCMethodInstance {

	public OCIPMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
