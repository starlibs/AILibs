package jaicore.logic.fol.structure;

/**
 * A variable parameter of a literal.
 *
 * @author mbunse, wever
 */
@SuppressWarnings("serial")
public class VariableParam extends LiteralParam {

	public VariableParam(final String name, final Type type) {
		super(name, type);
	}

	public VariableParam(final String name) {
		super(name);
	}

	public VariableParam(final VariableParam toBeCopied) {
		super(toBeCopied.getName(), toBeCopied.type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof VariableParam)) {
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		if (this.getType() != null) {
			return "<" + this.getName() + ":" + this.getType().getName() + ">";
		} else {
			return "<" + this.getName() + ":undefined>";
		}
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public void setType(final Type type) {
		this.type = type;
	}
}