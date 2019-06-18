package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
		return new HashCodeBuilder().append(this.getOperation()).append(this.getGrounding()).append(this.addList).append(this.deleteList).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof StripsAction)) {
			return false;
		}
		StripsAction other = (StripsAction) obj;
		return new EqualsBuilder().append(other.addList, this.addList).append(other.deleteList, this.deleteList).isEquals();
	}
}
