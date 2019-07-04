package ai.libs.jaicore.planning.classical.problems.ce;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;

@SuppressWarnings("serial")
public class CEOperation extends Operation {

	private final Map<CNFFormula, Monom> addLists;
	private final Map<CNFFormula, Monom> deleteLists;

	public CEOperation(final String name, final String params, final Monom precondition, final Map<CNFFormula, Monom> addLists, final Map<CNFFormula, Monom> deleteLists) {
		this(name, Arrays.asList(StringUtil.explode(params, ",")).stream().map(s -> new VariableParam(s.trim())).collect(Collectors.toList()), precondition, addLists,
				deleteLists);
	}

	public CEOperation(final String name, final List<VariableParam> params, final Monom precondition, final Map<CNFFormula, Monom> addLists, final Map<CNFFormula, Monom> deleteLists) {
		super(name, params, precondition);
		this.addLists = addLists;
		this.deleteLists = deleteLists;
	}

	public Map<CNFFormula, Monom> getAddLists() {
		return this.addLists;
	}

	public Map<CNFFormula, Monom> getDeleteLists() {
		return this.deleteLists;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.addLists).append(this.deleteLists).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CEOperation)) {
			return false;
		}
		CEOperation other = (CEOperation) obj;
		return new EqualsBuilder().append(other.addLists, this.addLists).append(other.deleteLists, this.deleteLists).isEquals();
	}

}
