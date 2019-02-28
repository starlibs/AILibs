package jaicore.planning.classical.problems.ceoc;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.ce.CEAction;

@SuppressWarnings("serial")
public class CEOCAction extends CEAction {

	public CEOCAction(CEOCOperation operation, Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
	}
	
	public CEOCOperation getOperation() {
		return (CEOCOperation)super.getOperation();
	}
}
