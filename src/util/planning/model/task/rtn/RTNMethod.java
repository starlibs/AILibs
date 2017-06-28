package util.planning.model.task.rtn;

import java.util.List;

import util.logic.Literal;
import util.logic.Monom;
import util.logic.VariableParam;
import util.planning.model.task.ceocstn.OCMethod;

public class RTNMethod extends OCMethod {

	public RTNMethod(String name, List<VariableParam> parameters, Literal task, Monom precondition, RTaskNetwork network, boolean lonely, List<VariableParam> outputs) {
		super(name, parameters, task, precondition, network, lonely, outputs);
	}
}