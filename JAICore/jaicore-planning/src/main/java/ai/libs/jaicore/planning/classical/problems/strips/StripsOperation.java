package ai.libs.jaicore.planning.classical.problems.strips;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
		return new HashCodeBuilder().append(super.hashCode()).append(this.addList).append(this.deleteList).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof StripsOperation)) {
			return false;
		}
		StripsOperation other = (StripsOperation) obj;
		return new EqualsBuilder().append(other.addList, this.addList).append(other.deleteList, this.deleteList).isEquals();

	}
}
