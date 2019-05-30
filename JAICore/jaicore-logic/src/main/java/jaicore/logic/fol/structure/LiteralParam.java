package jaicore.logic.fol.structure;

import java.io.Serializable;

/**
 * The parameter of a literal.
 *
 * @author mbunse
 */
@SuppressWarnings("serial")
public abstract class LiteralParam implements Serializable {

	private String name;
	protected Type type;

	/**
	 * @param name
	 *            The name of this parameter;
	 */
	public LiteralParam(final String name) {
		this.name = name;
	}

	/**
	 * @param name
	 *            The name of this parameter;
	 */
	public LiteralParam(final String name, final Type type) {
		this(name);
		this.setType(type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	/**
	 * It is with intention that the equals method does NOT check the type.
	 * We assume that the name of a parameter is sufficient to identify it.
	 * The type is rather optional to enable efficient processing in some contexts.
	 *
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		LiteralParam other = (LiteralParam) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public String getName() {
		return this.name;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

}
