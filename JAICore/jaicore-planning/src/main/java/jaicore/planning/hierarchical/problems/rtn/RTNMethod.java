package jaicore.planning.hierarchical.problems.rtn;

import java.util.List;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethod;

@SuppressWarnings("serial")
public class RTNMethod extends OCMethod {

	public RTNMethod(String name, List<VariableParam> parameters, Literal task, Monom precondition, RTaskNetwork network, boolean lonely, List<VariableParam> outputs) {
		super(name, parameters, task, precondition, network, lonely, outputs);
	}
}