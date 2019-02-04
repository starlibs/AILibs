package jaicore.planning.model.ceoc;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.model.conditional.CEAction;

@SuppressWarnings("serial")
public class CEOCAction extends CEAction {

	public CEOCAction(CEOCOperation operation, Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
	}
	
	public CEOCOperation getOperation() {
		return (CEOCOperation)super.getOperation();
	}
}
