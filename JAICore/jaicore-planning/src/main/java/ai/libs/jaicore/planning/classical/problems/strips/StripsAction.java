package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.core.Action;

@SuppressWarnings("serial")
public class StripsAction extends Action {

	private final Monom addList;
	private final Monom deleteList;

	public StripsAction(final StripsOperation operation, final Map<VariableParam, ConstantParam> grounding) {
		super(operation, grounding);
		this.addList = new Monom(operation.getAddList(), grounding);
		this.deleteList = new Monom(operation.getDeleteList(), grounding);
	}

	public Monom getAddList() {
		return this.addList;
	}

	public Monom getDeleteList() {
		return this.deleteList;
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
		StripsAction other = (StripsAction) obj;
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
