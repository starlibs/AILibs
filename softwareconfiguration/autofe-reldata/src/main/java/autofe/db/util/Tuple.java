package autofe.db.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Tuple<T, U> {

	private T t;

	private U u;

	public Tuple(T t, U u) {
		super();
		this.t = t;
		this.u = u;
	}

	public T getT() {
		return t;
	}

	public void setT(T t) {
		this.t = t;
	}

	public U getU() {
		return u;
	}

	public void setU(U u) {
		this.u = u;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(t).append(u).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		return new EqualsBuilder().append(t, other.t).append(u, other.u).isEquals();
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", t.toString(), u.toString());
	}

}
