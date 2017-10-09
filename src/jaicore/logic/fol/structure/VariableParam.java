package jaicore.logic.fol.structure;

/**
 * A variable parameter of a literal.
 * 
 * @author mbunse, wever
 */
public class VariableParam extends LiteralParam {
	
	private Type type;

	public VariableParam(String name, Type type) {
		super(name);
		this.type = type;
	}

	public VariableParam(String name) {
		super(name);
	}

	public VariableParam(VariableParam toBeCopied) {
		super(toBeCopied.getName());
		this.type = toBeCopied.type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VariableParam))
			return false;
		return super.equals(obj);
	}

	@Override
	public String toString() {
		if (getType() != null) {
			return "<" + this.getName() + ":" + getType().getName() + ">";
		} else {
			return "<" + this.getName() + ":undefined>";
		}
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}