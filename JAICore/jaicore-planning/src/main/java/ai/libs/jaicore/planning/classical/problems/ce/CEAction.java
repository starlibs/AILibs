package ai.libs.jaicore.planning.classical.problems.ce;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.core.Action;

@SuppressWarnings("serial")
public class CEAction extends Action {
	public CEAction(final CEOperation operation, final Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
	}

	public Map<CNFFormula, Monom> getAddLists() {
		Map<CNFFormula, Monom> addLists = new HashMap<>();
		CEOperation operation = ((CEOperation)this.getOperation());
		Map<VariableParam, ConstantParam> grounding = this.getGrounding();
		for (CNFFormula key : operation.getAddLists().keySet()) {
			CNFFormula condition = new CNFFormula(key, grounding);
			if (!condition.isObviouslyContradictory()) {
				addLists.put(condition, new Monom(operation.getAddLists().get(key), grounding));
			}
		}
		return addLists;
	}

	public Map<CNFFormula, Monom> getDeleteLists() {
		CEOperation operation = ((CEOperation)this.getOperation());
		Map<VariableParam, ConstantParam> grounding = this.getGrounding();
		Map<CNFFormula, Monom> deleteLists = new HashMap<>();
		for (CNFFormula key : operation.getDeleteLists().keySet()) {
			CNFFormula condition = new CNFFormula(key, grounding);
			if (!condition.isObviouslyContradictory()) {
				deleteLists.put(condition, new Monom(operation.getDeleteLists().get(key), grounding));
			}
		}
		return deleteLists;
	}
}
