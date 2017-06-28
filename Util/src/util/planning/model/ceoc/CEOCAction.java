package util.planning.model.ceoc;

import java.util.Map;

import util.logic.ConstantParam;
import util.logic.VariableParam;
import util.planning.model.conditional.CEAction;

@SuppressWarnings("serial")
public class CEOCAction extends CEAction {

	public CEOCAction(CEOCOperation operation, Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
	}
	
	public CEOCOperation getOperation() {
		return (CEOCOperation)super.getOperation();
	}
}
