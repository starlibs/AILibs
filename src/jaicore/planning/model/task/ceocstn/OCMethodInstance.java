package jaicore.planning.model.task.ceocstn;

import java.util.Map;

import jaicore.logic.ConstantParam;
import jaicore.logic.VariableParam;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;

public class OCMethodInstance extends MethodInstance {

	public OCMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
