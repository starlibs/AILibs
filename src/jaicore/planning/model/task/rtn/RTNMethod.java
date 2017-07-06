package jaicore.planning.model.task.rtn;

import java.util.List;

import jaicore.logic.Literal;
import jaicore.logic.Monom;
import jaicore.logic.VariableParam;
import jaicore.planning.model.task.ceocstn.OCMethod;

public class RTNMethod extends OCMethod {

	public RTNMethod(String name, List<VariableParam> parameters, Literal task, Monom precondition, RTaskNetwork network, boolean lonely, List<VariableParam> outputs) {
		super(name, parameters, task, precondition, network, lonely, outputs);
	}
}