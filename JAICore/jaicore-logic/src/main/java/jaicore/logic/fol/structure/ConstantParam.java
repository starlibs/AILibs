package jaicore.logic.fol.structure;

/**
 * The constant parameter of a literal.
 *
 * @author mbunse
 */
@SuppressWarnings("serial")
public class ConstantParam extends LiteralParam {

	private boolean variablesMayBeUnifiedWithThisConstant;

	public ConstantParam(final String name, final boolean pVariablesMayBeUnifiedWithThisConstant) {
		super(name);
		this.variablesMayBeUnifiedWithThisConstant = pVariablesMayBeUnifiedWithThisConstant;
	}

	public ConstantParam(final String name) {
		this(name, true);
	}

	public ConstantParam(final String name, final Type type) {
		this(name);
		this.type = type;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ConstantParam)) {
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public boolean variablesMayBeUnifiedWithThisConstant() {
		return this.variablesMayBeUnifiedWithThisConstant;
	}
}
