package jaicore.logic;

/**
 * A variable parameter of a literal.
 * 
 * @author fmohr
 */
public class VariableParam extends LiteralParam {

	public VariableParam(String name) {
		super(name);
	}

	public VariableParam(VariableParam toBeCopied) {
		super(toBeCopied.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VariableParam))
			return false;
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "<" + this.getName() + ">";
	}
}