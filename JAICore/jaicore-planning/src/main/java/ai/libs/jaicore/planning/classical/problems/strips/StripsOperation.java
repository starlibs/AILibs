package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.List;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;

@SuppressWarnings("serial")
public class StripsOperation extends Operation {

	private final Monom addList;
	private final Monom deleteList;

	public StripsOperation(final String name, final List<VariableParam> params, final Monom precondition, final Monom addList, final Monom deleteList) {
		super(name, params, precondition);
		this.addList = addList;
		this.deleteList = deleteList;
	}

	public Monom getAddList() {
		return this.addList;
	}

	public Monom getDeleteList() {
		return this.deleteList;
	}

	@Override
	public String toString() {
		return "StripsOperation [precondition=" + this.getPrecondition() + ", addList=" + this.addList + ", deleteList=" + this.deleteList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.addList == null) ? 0 : this.addList.hashCode());
		result = prime * result + ((this.deleteList == null) ? 0 : this.deleteList.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		StripsOperation other = (StripsOperation) obj;
		if (this.addList == null) {
			if (other.addList != null) {
				return false;
			}
		} else if (!this.addList.equals(other.addList)) {
			return false;
		}
		if (this.deleteList == null) {
			if (other.deleteList != null) {
				return false;
			}
		} else if (!this.deleteList.equals(other.deleteList)) {
			return false;
		}
		return true;
	}
}
