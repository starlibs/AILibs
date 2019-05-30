package autofe.db.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Tuple<T, U> {

	private T t;

	private U u;

	public Tuple(final T t, final U u) {
		super();
		this.t = t;
		this.u = u;
	}

	public T getT() {
		return this.t;
	}

	public void setT(final T t) {
		this.t = t;
	}

	public U getU() {
		return this.u;
	}

	public void setU(final U u) {
		this.u = u;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.t).append(this.u).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Tuple)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		return new EqualsBuilder().append(this.t, other.t).append(this.u, other.u).isEquals();
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", this.t.toString(), this.u.toString());
	}

}
