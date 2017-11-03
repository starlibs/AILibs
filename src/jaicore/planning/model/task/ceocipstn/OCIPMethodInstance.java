package jaicore.planning.model.task.ceocipstn;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.model.task.ceocstn.OCMethodInstance;
import jaicore.planning.model.task.stn.Method;

@SuppressWarnings("serial")
public class OCIPMethodInstance extends OCMethodInstance {

	public OCIPMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
