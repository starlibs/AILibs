package jaicore.planning.model.task.ceocstn;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;

public class OCMethodInstance extends MethodInstance {

	public OCMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
