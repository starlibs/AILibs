package jaicore.planning.hierarchical.problems.ceocipstn;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethodInstance;
import jaicore.planning.hierarchical.problems.stn.Method;

@SuppressWarnings("serial")
public class OCIPMethodInstance extends OCMethodInstance {

	public OCIPMethodInstance(Method method, Map<VariableParam, ConstantParam> grounding) {
		super(method, grounding);
	}
}
