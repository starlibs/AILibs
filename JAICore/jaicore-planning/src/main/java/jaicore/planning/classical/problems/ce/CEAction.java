package jaicore.planning.classical.problems.ce;

import java.util.HashMap;
import java.util.Map;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.core.Action;

@SuppressWarnings("serial")
public class CEAction extends Action {
	public CEAction(CEOperation operation, Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
	}

	public Map<CNFFormula, Monom> getAddLists() {
		Map<CNFFormula, Monom> addLists = new HashMap<>();
		CEOperation operation = ((CEOperation)getOperation());
		Map<VariableParam, ConstantParam> grounding = getGrounding();
		for (CNFFormula key : operation.getAddLists().keySet()) {
			CNFFormula condition = new CNFFormula(key, grounding);
			if (!condition.isObviouslyContradictory())
				addLists.put(condition, new Monom(operation.getAddLists().get(key), grounding));
		}
		return addLists;
	}

	public Map<CNFFormula, Monom> getDeleteLists() {
		CEOperation operation = ((CEOperation)getOperation());
		Map<VariableParam, ConstantParam> grounding = getGrounding();
		Map<CNFFormula, Monom> deleteLists = new HashMap<>();
		for (CNFFormula key : operation.getDeleteLists().keySet()) {
			CNFFormula condition = new CNFFormula(key, grounding);
			if (!condition.isObviouslyContradictory())
				deleteLists.put(condition, new Monom(operation.getDeleteLists().get(key), grounding));
		}
		return deleteLists;
	}
}
