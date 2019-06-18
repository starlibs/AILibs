package ai.libs.jaicore.logic.fol.structure;

/**
 * The constant parameter of a literal.
 * 
 * @author mbunse
 */
@SuppressWarnings("serial")
public class ConstantParam extends LiteralParam {

	private boolean variablesMayBeUnifiedWithThisConstant;

	public ConstantParam(String name, boolean pVariablesMayBeUnifiedWithThisConstant) {
		super(name);
		this.variablesMayBeUnifiedWithThisConstant = pVariablesMayBeUnifiedWithThisConstant;
	}

	public ConstantParam(String name) {
		this(name, true);
	}

	public ConstantParam(String name, Type type) {
		this(name);
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConstantParam))
			return false;
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public boolean variablesMayBeUnifiedWithThisConstant() {
		return variablesMayBeUnifiedWithThisConstant;
	}
}
