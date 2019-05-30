package jaicore.planning.classical.problems.strips;

import java.util.Map;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.core.Action;

@SuppressWarnings("serial")
public class StripsAction extends Action {

	private final Monom addList, deleteList;

	public StripsAction(StripsOperation operation, Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
		this.addList = new Monom(operation.getAddList(), grounding);
		this.deleteList = new Monom(operation.getDeleteList(), grounding);
	}

	public Monom getAddList() {
		return addList;
	}

	public Monom getDeleteList() {
		return deleteList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((addList == null) ? 0 : addList.hashCode());
		result = prime * result + ((deleteList == null) ? 0 : deleteList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StripsAction other = (StripsAction) obj;
		if (addList == null) {
			if (other.addList != null)
				return false;
		} else if (!addList.equals(other.addList))
			return false;
		if (deleteList == null) {
			if (other.deleteList != null)
				return false;
		} else if (!deleteList.equals(other.deleteList))
			return false;
		return true;
	}
}
