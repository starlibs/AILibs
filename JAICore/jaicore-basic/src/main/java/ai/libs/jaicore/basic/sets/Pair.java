package ai.libs.jaicore.basic.sets;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Pair<X,Y> implements Serializable {

	private static final long serialVersionUID = 5570679807997613881L;

	@JsonProperty
	private X x;
	@JsonProperty
	private Y y;

	public Pair(@JsonProperty("x") final X x, @JsonProperty("y") final Y y) {
		super();
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return this.x;
	}

	public Y getY() {
		return this.y;
	}

	@Override
	public String toString() {
		return "<" + this.x + ", " + this.y + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.x == null) ? 0 : this.x.hashCode());
		result = prime * result + ((this.y == null) ? 0 : this.y.hashCode());
		return result;
	}

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
		@SuppressWarnings("unchecked")
		Pair<X, Y> other = (Pair<X, Y>) obj;
		if (this.x == null) {
			if (other.x != null) {
				return false;
			}
		} else if (!this.x.equals(other.x)) {
			return false;
		}
		if (this.y == null) {
			if (other.y != null) {
				return false;
			}
		} else if (!this.y.equals(other.y)) {
			return false;
		}
		return true;
	}
}
