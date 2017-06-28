package util.logic;

import java.io.Serializable;

import util.basic.ObjectSizeFetcher;

/**
 * The parameter of a literal.
 * 
 * @author mbunse
 */
public abstract class LiteralParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5977769530543978156L;
	private final String name;

	/**
	 * @param name
	 *            The name of this parameter;
	 */
	public LiteralParam(String name) {
		this.name = name;
	}

	/**
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiteralParam other = (LiteralParam) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public long getMemory() {
		return ObjectSizeFetcher.getObjectSize(name);
	}
}
