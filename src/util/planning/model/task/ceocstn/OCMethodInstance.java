package util.planning.model.task.ceocstn;

import java.util.Map;

import util.logic.ConstantParam;
import util.logic.VariableParam;
import util.planning.model.task.stn.Method;
import util.planning.model.task.stn.MethodInstance;

public class OCMethodInstance extends MethodInstance {

	public OCMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
