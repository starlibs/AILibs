package jaicore.planning.classical.problems.strips;

import java.util.List;

import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

@SuppressWarnings("serial")
public class StripsOperation extends Operation {
	
	private final Monom addList, deleteList;

	public StripsOperation(String name, List<VariableParam> params, Monom precondition, Monom addList, Monom deleteList) {
		super(name, params, precondition);
		this.addList = addList;
		this.deleteList = deleteList;
	}

	public Monom getAddList() {
		return addList;
	}

	public Monom getDeleteList() {
		return deleteList;
	}

	@Override
	public String toString() {
		return "StripsOperation [precondition=" + getPrecondition() + ", addList=" + addList + ", deleteList=" + deleteList + "]";
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
		StripsOperation other = (StripsOperation) obj;
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
